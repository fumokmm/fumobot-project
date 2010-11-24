package action

import java.util.logging.Logger
import com.google.appengine.api.datastore.*
import static com.google.appengine.api.datastore.FetchOptions.Builder.*

import bean.*
import dao.*
import util.*
import static util.DaoUtil.*
import hatenahaiku4j.util.*

/**
 * キーワードメーカーアクション
 */
class KeywordMakerAction {
	static { ext.Extension } // メソッド拡張

	/** キーワード最大文字長 */
	static final def KEYWORD_MAX_SIZE = 140
	/** キーワード作成数(デフォルト=5) */
	static final def MAKING_NUM = 5

	/** ボットのID */
	def botId
	/** 解析対象ステータス */
	def statusEntity
	/** キーワードメーカーDao */
	def makerDao
	/** キーワードメーカーDaoを取得 */
	def getMakerDao() {
		if (this.makerDao) return this.makerDao
		this.makerDao = new KeywordMakerDao()
		return this.makerDao
	}

	/** テンプレート */
	def template
	/** 伏字のリスト */
	def secretMarkList
	/** 文字数指定の伏字のリスト */
	def sizedSecretMarkList

	/** フレーズのリスト */
	def phraseList

	/** 更新用テンプレートのリスト */
	def tempTemplates
	/** 更新用フレーズのリスト */
	def tempPhraseList

	/** 作成されたキーワードのリスト */
	def makedKeywordList

	/** ロガー */
	static def log = Logger.getLogger(KeywordMakerDao.class.name)

	/**
	 * コンストラクタ
	 *
	 * @param botId ボットID
	 * @param statusEntity (KIND='HaikuStatus')
	 */
	KeywordMakerAction(botId, statusEntity) {
		this.botId        = botId
		this.statusEntity = statusEntity
		init()	// 初期化
		if (isAvailable()) {
			log.info("${statusEntity[STATUS_ID]} 開始 ${statusEntity[TEXT].value}")
			if (template)   log.info("テンプレート: ${template}")
			if (phraseList) log.info("キーフレーズ: ${phraseList}")

			// 生成
			make()

			log.info("${statusEntity[STATUS_ID]} 終了")
		}
	}

	/**
	 * 利用可能かどうか
	 */
	def isAvailable() {
		template || phraseList
	}

	/**
	 * 初期化判定します。
	 */
	def init() {
		// botのエントリだったら何もしない
		if (this.statusEntity[USER_ID] == this.botId) {
			return
		}
		// idページの場合、キーワードメーカーパターンかどうか判定
		if (this.statusEntity[KEYWORD] == 'id:' + this.botId) {
			if (TextUtil.checkPattern(this.statusEntity[TEXT].value, TextUtil.KEYWORD_MAKER_PATTERN)) {
				// ※キーワードメーカー※を削る
				this.statusEntity[TEXT] = new Text(this.statusEntity[TEXT].value.replaceFirst(TextUtil.KEYWORD_MAKER_PATTERN, ''))
			} else {
				return
			}
		}	
		// 無視パターンならなにもせず終了
		if (TextUtil.checkIgnore(this.statusEntity[TEXT].value)) {
			return
		}
		// エントリに 'http' が含まれていたらなにもせず終了
		if (this.statusEntity[TEXT].value.contains('http')) {
			return
		}

		if (statusEntity[TEXT].value) {
			// フレーズリストを取得
			this.phraseList = parsePhraseList(statusEntity[TEXT].value)
			// 取得したフレーズリストに伏字パターンが含まれている場合、終了
			if (this.phraseList.join() =~ /${TextUtil.SECRET_MARKS_PATTERN}/) {
				this.phraseList = null
				return
			}

			// フレーズがない場合
			if (!this.phraseList) {
				// 伏字のリストを取得
				this.secretMarkList = parseSecretMarkList(statusEntity[TEXT].value)
				// 文字数指定の伏字のリストを取得
				this.sizedSecretMarkList = parseSizedSecretMarkList(statusEntity[TEXT].value)

				if (!this.secretMarkList) {
					// 伏字もないなら終了
					return
				} else {
					// テンプレートを取得
					this.template = statusEntity[TEXT].value.flatten()
				}
			}
		}
	}

	/**
	 * キーワードを作成する。
	 */
	def make() {
		assert isAvailable()

		if (this.template) {
			makeByTemplate()	// テンプレートがある場合
		} else if (this.phraseList) {
			makeByKeyPhrase()	// フレーズリストがある場合
		}

		// 最大文字サイズに削る
		for (i in 0..<this.makedKeywordList.size()) {
			if (this.makedKeywordList[i].size() > KEYWORD_MAX_SIZE ) {
				this.makedKeywordList[i] = this.makedKeywordList[i][0..<KEYWORD_MAX_SIZE]
			}
		}
	}

	/**
	 * テンプレートから作る
	 */
	def makeByTemplate() {
		assert this.template

		// テンプレートの伏字に対応するキーフレーズを取得してきてマップにする
		def translator = [:]
		MAKING_NUM.times {
			(this.secretMarkList - this.sizedSecretMarkList).each {
			    translator.get(it, []).addAll getMakerDao().getRandomKeyPhrase(0)[TITLE]
			}
			this.sizedSecretMarkList.each {
			    translator.get(it, []).addAll getMakerDao().getRandomKeyPhrase(it.size())[TITLE]
			}
		}
		// 更新用に保持
		this.tempPhraseList = translator*.value.flatten().unique()

		// 必要数のテンプレートを用意する
		def templates = [this.template] * MAKING_NUM
		
		// 作成済みキーワードリストに設定
		this.makedKeywordList = templates.collect{ tmpl ->
			translator.each {
				tmpl = tmpl.replace(it.key, it.value.pop())
			}
			tmpl
		}
	}

	/**
	 * キーフレーズから作る
	 */
	def makeByKeyPhrase() {
		assert this.phraseList

		// 必要数のテンプレートを用意して更新用に保持
		this.tempTemplates = getMakerDao().getRandomTemplate(this.phraseList.size(), MAKING_NUM).collect{ it[TITLE] }

		// テンプレートの伏字部分をキーフレーズで埋める

		// 作成済みキーワードリストに設定
		this.makedKeywordList = this.tempTemplates.collect { tmpl ->
			// フレーズリストをシャッフル
			this.phraseList = this.phraseList.shuffle()
			// テンプレートに当てはめる
			parseSecretMarkList(tmpl).eachWithIndex { secretMark, idx ->
				tmpl = tmpl.replace(secretMark, this.phraseList[idx])
			}
			tmpl
		}
	}

	/**
	 * 文字列からフレーズのリストを取得。
	 */
	static def parsePhraseList(String str) {
		assert str.class.name == 'java.lang.String', 'Stringのはず in #parsePhraseList'

		str.flatten()													// フラット化して
			.findAll(/${TextUtil.KEYWORD_PATTERN}/){ _0, _1 -> _1 }		// フレーズのリストを検索して
			.unique()													// 重複を除去して
			.sort{ a, b -> b.size() - a.size() }						// 大きいほう優先で並べ替え
	}

	/**
	 * 文字列から伏字のリストを取得。
	 */
	static def parseSecretMarkList(String str) {
		assert str.class.name == 'java.lang.String', 'Stringのはず in #parsePhraseList'

		str.flatten()										// フラット化して
			.findAll(/(${TextUtil.SECRET_MARKS_PATTERN})/)	// リストを検索して
			.unique()										// 重複を除去して
			.sort{ a, b -> b.size() - a.size() }			// 大きいほう優先で並べ替え
	}

	/**
	 * 文字列から伏字(文字数指定)のリストを取得。
	 */
	static def parseSizedSecretMarkList(String str) {
		assert str.class.name == 'java.lang.String', 'Stringのはず in #parsePhraseList'

		str.flatten()												// フラット化して
			.findAll(/(${TextUtil.SECRET_MARKS_SIZED_PATTERN})/)	// リストを検索して
			.unique()												// 重複を除去して
			.sort{ a, b -> b.size() - a.size() }					// 大きいほう優先で並べ替え
	}

	/**
	 * 現在の状態でデータストアを更新する。
	 */
	def update() {
		assert isAvailable()

		int userCount = 0

		if (this.template) {
			// テンプレートを登録＆更新
			getMakerDao().getTemplateForUpdate(this.template, this.statusEntity[USER_ID]) { entity ->
				if (entity[COUNT_OF_USING].toInteger() == 0) {
					userCount++
				}
				entity[COUNT_OF_USING]++
				entity[_UUID] = TextUtil.getUUID()
				entity[UPDATED_AT] = DateUtil.now
				log.info("テンプレート: ${this.template} を更新しました。COUNT_OF_USING は ${entity[COUNT_OF_USING].toInteger()} になりました。")
			}

		} else if (this.phraseList) {
			this.phraseList.each { phrase ->
				// キーフレーズを登録＆更新
				getMakerDao().getKeyPhraseForUpdate(phrase, this.statusEntity[USER_ID]) { entity ->
					if (entity[COUNT_OF_USING].toInteger() == 0) {
						userCount++
					}
					entity[COUNT_OF_USING]++
					entity[_UUID] = TextUtil.getUUID()
					entity[UPDATED_AT] = DateUtil.now
					log.info("キーフレーズ: ${phrase} を更新しました。COUNT_OF_USING は ${entity[COUNT_OF_USING].toInteger()} になりました。")
				}
			}
		}
		
		if (this.tempTemplates) {
			(this.tempTemplates - this.template).each { tmpl ->
				// テンプレートを登録＆更新
				getMakerDao().getTemplateForUpdate(tmpl, this.statusEntity[USER_ID]) { entity ->
					entity[COUNT_OF_USING]++
					entity[_UUID] = TextUtil.getUUID()
					entity[UPDATED_AT] = DateUtil.now
					log.info("テンプレート: ${tmpl} を更新しました。COUNT_OF_USING は ${entity[COUNT_OF_USING].toInteger()} になりました。")
				}
			}
		}

		if (this.tempPhraseList) {
			(this.tempPhraseList - this.phraseList).each { phrase ->
				// キーフレーズを登録＆更新
				getMakerDao().getKeyPhraseForUpdate(phrase, this.statusEntity[USER_ID]) { entity ->
					entity[COUNT_OF_USING]++
					entity[_UUID] = TextUtil.getUUID()
					entity[UPDATED_AT] = DateUtil.now
					log.info("キーフレーズ: ${phrase} を更新しました。COUNT_OF_USING は ${entity[COUNT_OF_USING].toInteger()} になりました。")
				}
			}
		}

		// キーワードメーカーのカウントアップ
		new UserInfoDao(this.statusEntity[USER_ID]).countUp(COUNT_OF_KEYWORD_MAKER, userCount)
		log.info("${this.statusEntity[USER_ID]} さんのキーワードメーカーのカウントを ${userCount} アップさせました。")
	}

	/**
	 * コマンドを生成する。
	 */
	def createCommand() {
		assert isAvailable()

		// ユーザの利用回数
		def count = new UserInfoDao(this.statusEntity[USER_ID]).getCount(COUNT_OF_KEYWORD_MAKER)

		// 投稿する内容
		def text = new TemplateReader().read('keyword/keyword_maker_simple.tmpl', [
			'userName'	: HatenaUtil.getUserName(this.statusEntity[USER_ID]) ?: this.statusEntity[USER_ID],
			'count'		: count,
			'list'		: makedKeywordList.collect{
						      HaikuURL.byKeyword(it).getEscapedLink()
						  },
		])

		// 作成したキーワードを返信するコマンドを登録
		def param = [
			'service'			: 'haiku',
			'priority'			: 100,
			'createdAt'			: DateUtil.now,
			'who'				: this.botId,
			'command'			: 'reply',
			'inReplyToStatusId'	: statusEntity[STATUS_ID],
			'text'				: text
		]
		new CommandQueueDao().addCommand(param)
	}
}
