import com.google.appengine.api.datastore.*
import java.util.logging.Logger
import dao.*
import util.*
import static util.DaoUtil.*
import hatenahaiku4j.*
import hatenahaiku4j.util.*

ext.Extension	// メソッド拡張

// 定数など
BOT_ID			= 'fumobot'	// ボットID
SERVICE_HAIKU	= 'haiku'	// サービス
KEYWORD_URANAI	= 'キーワード占い'

// はてなハイクAPI
apiLight = new HatenaHaikuAPILight()

def statusDao = new HaikuStatusDao()

// 占いするよ
uranaiSuruyo()

// キーワードリストを用意しておく
hotList = collectHotList()

// ここからメイン処理
try {
	statusDao.getStatusByKeyword('キーワード占い', 4, FUMOBOT_FLG).each { statusDS ->
		if (statusDS) {
			// コマンド作成
			makeCommand(statusDS)
			// ふもぼフラグを立てる
			statusDao.setFlgOn statusDS[STATUS_ID], FUMOBOT_FLG
		}
	}
} catch(Exception e) {
	e.printStackTrace()
}

/**
 * コマンド作成します。
 * @param statusDS データストアのステータス
 */
def makeCommand(statusDS) {
	// 自分の投稿だったらコマンドを作成しない
	if (statusDS[USER_ID] == BOT_ID) {
		println "自分の投稿だったのでコマンドを作成なし<br/>"
		return
	}

	// 先頭が「※」の場合はコマンドを作成しない
	if (TextUtil.checkIgnore(statusDS[TEXT].value)) {
		println "先頭が「※」だったのでコマンド作成なし<br/>"
		return
	}

	// 返信でなかった場合
	if (!statusDS[IN_REPLY_TO_STATUS_ID]) {
		println "返信でなかったのでコマンド登録します<br/>"
		// コマンド登録
		uranai(statusDS)
		return
	}

	// 自分に対する返信だった場合
	if (statusDS[IN_REPLY_TO_USER_ID] == BOT_ID) {
		println "自分に対する返信だったのでコマンド登録します<br/>"
		try {
			def replySrc = apiLight.getStatus(statusDS[IN_REPLY_TO_STATUS_ID])
			// 返信元の返信元の投稿者がふもぼの場合
			if (replySrc.userId == BOT_ID) {
				if (replySrc.inReplyToStatusId && replySrc.inReplyToUserId == statusDS[USER_ID]) {
					// その返信元が「占い結果」だった場合、お礼を言う
					//　「占い結果」はふもぼ且つ返信先がある投稿
					orei(statusDS)

				} else {
					// その返信元が「占いするよ～」だった場合は占いする
					// 「占いするよ～」はふもぼ且つ返信先がない投稿
					uranai(statusDS)
				}
			}
		} catch(e) {
			println "もうすでに削除されているかもしれないため、登録できませんでした<br/>"
		}
		return
	}
}

/**
 * 占いをしてコマンドを登録する。
 * @param statusDS データストアのステータス
 */
def uranai(statusDS) {
	def list = hotList[0..<5]
	hotList = hotList - list

	def luckey = list[0]
	def up     = list[1]
	def star   = list[2]
	def down   = list[3]
	def help   = list[4]

	// ユーザ情報を取得
	def userInfoDao = new UserInfoDao(statusDS[USER_ID])
	// カウントを取得
	int count = (userInfoDao.getUserInfo()[COUNT_OF_KEYWORD_URANAI] as int) + 1

	// 占いの内容
	def text = new TemplateReader().read('fortune/fortune_telling.tmpl', [
		'userName'	: HatenaUtil.getUserName(statusDS[USER_ID]) ?: statusDS[USER_ID],
		'count'		: count,
		'luckey'	: HaikuURL.byKeyword(luckey).getEscapedLink(),
		'up'		: HaikuURL.byKeyword(up).getEscapedLink(),
		'star'		: HaikuURL.byKeyword(star).getEscapedLink(),
		'down'		: HaikuURL.byKeyword(down).getEscapedLink(),
		'help'		: HaikuURL.byKeyword(help).getEscapedLink(),
	])

	// 占いするコマンドを登録
	def param = [
		'service'	: SERVICE_HAIKU,
		'priority'	: 100,
		'createdAt'	: DateUtil.now,
		'who'		: BOT_ID,
		'command'	: 'reply',
		'inReplyToStatusId'	: statusDS[STATUS_ID],
		'text'		: text
	]
	new CommandQueueDao().addCommand(param)
	// 占いのカウントアップ
	userInfoDao.countUp(COUNT_OF_KEYWORD_URANAI)
}

/**
 * お礼のコマンドを登録する。
 * @param statusDS データストアのステータス
 */
def orei(statusDS) {
	// 「ありがとう」があるかチェックする
	String replyTemplate = TextUtil.checkThanks(statusDS[TEXT].value) ?
		'fortune/reply_to_fortune_telling_you_are_welcome.tmpl' :
		'fortune/reply_to_fortune_telling_thanks.tmpl'

	// お礼の内容
	def text = new TemplateReader().read(replyTemplate, [
		'userName'	: HatenaUtil.getUserName(statusDS[USER_ID]) ?: statusDS[USER_ID],
	])

	// お礼をするコマンドを登録
	def param = [
		'service'	: SERVICE_HAIKU,
		'priority'	: 100,
		'createdAt'	: DateUtil.now,
		'who'		: BOT_ID,
		'command'	: 'reply',
		'inReplyToStatusId'	: statusDS[STATUS_ID],
		'text'		: text
	]
	new CommandQueueDao().addCommand(param)
}

/**
 * ホットリストを集めます。失敗したら空リストを返却する。
 */
def collectHotList() {
	try {
		apiLight
		.getHotKeywordList()	// ホットキーワードを取得
		.collect{ it.relatedKeywords << it.title }	// 関連キーワード＋タイトルのリストにして
		.flatten()	// くっつけた結果を全部平らなリストにして
		.unique()	// それをユニークにして
		.shuffle()	// シャッフルした結果を返却
	} catch(e) {
		request.setAttribute 'message', 'ホットキーワードリスト取得失敗'
		forward '/error.gtpl'
	}
}

/**
 * 占いするよ～
 */
def uranaiSuruyo() {
	def since = apiLight.getKeywordTimeline(KEYWORD_URANAI).first().createdAt
	println "更新日時: ${since}<br/>"

	// 経過時間（分）
	int elapsedMin = ((DateUtil.now.time - since.time) / (1000 * 60)) as int
	if (elapsedMin > 300) {
		// 占いするよ～コマンドを登録
		def param = [
			'service'	: SERVICE_HAIKU,
			'priority'	: 999,
			'createdAt'	: DateUtil.now,
			'who'		: BOT_ID,
			'command'	: 'entry',
			'keyword'	: KEYWORD_URANAI,
			'text'		: new TemplateReader().read('fortune/uranai_suruyo.txt')
		]
		new CommandQueueDao().addCommand(param)

		request.setAttribute 'message', '６時間以上経過していたので占いするよ～コマンドを登録'
		forward '/success.gtpl'
	}
}
