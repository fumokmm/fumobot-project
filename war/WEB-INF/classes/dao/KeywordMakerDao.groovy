package dao

import java.util.logging.Logger
import com.google.appengine.api.datastore.*
import static com.google.appengine.api.datastore.FetchOptions.Builder.*

import action.*
import util.*
import static util.DaoUtil.*
import hatenahaiku4j.util.*

/**
 * キーワードメーカーに関するDAO
 *
 * -------------------------------------------------
 * [kind]
 *   KeywordTemplate
 *
 * [field] (★…Key)
 *      util.DaoUtil.TITLE          テンプレートの内容
 *      util.DaoUtil.SIZE           テンプレートの内容のサイズ(文字数)
 *   ★ util.DaoUtil.HASH           テンプレートの内容のハッシュ
 *      util.DaoUtil._UUID          このテンプレートのユニークなID(アクセス時に更新)
 *      util.DaoUtil.KEY_PHRASE_NUM フレーズ数
 *      util.DaoUtil.COUNT_OF_USING 使用回数
 *      util.DaoUtil.UPDATED_AT     最終更新日時(UUIDが変更された時間)
 *      util.DaoUtil.CREATED_AT     作成日時
 *      util.DaoUtil.USER_ID        作成者
 * -------------------------------------------------
 * [kind]
 *   KeywordKeyPhrase
 *
 * [field] (★…Key)
 *      util.DaoUtil.TITLE          フレーズの内容
 *      util.DaoUtil.SIZE           フレーズの内容のサイズ(文字数)
 *   ★ util.DaoUtil.HASH           フレーズの内容のハッシュ
 *      util.DaoUtil._UUID          このフレーズのユニークなID(アクセス時に更新)
 *      util.DaoUtil.COUNT_OF_USING 使用回数
 *      util.DaoUtil.UPDATED_AT     最終更新日時(UUIDが変更された時間)
 *      util.DaoUtil.CREATED_AT     作成日時
 *      util.DaoUtil.USER_ID        作成者
 * -------------------------------------------------
 */
class KeywordMakerDao {
	static { ext.Extension } // メソッド拡張
	//------------------------------------------------
	// Constants

	/** このDAOで扱うKIND(Template) */
	static final String KIND_T = 'KeywordTemplate'
	/** このDAOで扱うKIND(KeyPhrase) */
	static final String KIND_P = 'KeywordKeyPhrase'

	//------------------------------------------------
	// Property of this class
	/** データストアサービス */
	DatastoreService datastoreService
	/** ロガー */
	static def log = Logger.getLogger(KeywordMakerDao.class.name)

	//------------------------------------------------
	// Constructor

	/**
	 * コンストラクタ
	 */
	KeywordMakerDao() {
		this.datastoreService = DatastoreServiceFactory.getDatastoreService()
	}

	//------------------------------------------------
	// Methods

	/**
	 * ランダムにテンプレートを取得する。
	 * 取得数より少ない場合、取得した分だけ返却する。
	 *
	 * @param keyPhraseNum キーフレーズ数
	 * @param num 取得数(初期値:1)
	 * @return KIND=KeywordTemplate
	 */
	def getRandomTemplate(keyPhraseNum, num = 1) {
		def uuid = TextUtil.getUUID()
		def entityList = []

		// とりあえず小さいか同じほうで取得してみる
		// select * from KeywordTemlate
		// where keyPhraseNum = キーフレーズ数 and
		//       uuid <= UUID
		// order by uuid desc
		
		def q = new Query(KIND_T)
				.addFilter(KEY_PHRASE_NUM, Query.FilterOperator.EQUAL, keyPhraseNum) // キーフレーズ数
				.addFilter(_UUID, Query.FilterOperator.LESS_THAN_OR_EQUAL, uuid) // uuidが指定より小さいか同じもの
				.addSort(_UUID, Query.SortDirection.DESCENDING)					 // 指定のuuidに一番近いもの
		entityList.addAll datastoreService.prepare(q).asList(withLimit(num).offset(0))

		if (entityList.size() == num) {
			// 指定数分取得できている場合返却
			return entityList
		} else {
			// 指定数分取得できていない場合、大きいほうで取得してみる
			// select * from KeywordKeyPhrase
			// where keyPhraseNum = キーフレーズ数 and
			//       uuid > UUID
			// order by uuid asc
			
			def remain = num - entityList.size()	// 残りいくつ必要か
			q = new Query(KIND_T)
					.addFilter(KEY_PHRASE_NUM, Query.FilterOperator.EQUAL, keyPhraseNum) // キーフレーズ数
					.addFilter(_UUID, Query.FilterOperator.GREATER_THAN, uuid) // uuidが指定より大きいもの
					.addSort(_UUID, Query.SortDirection.ASCENDING)			   // 指定のuuidに一番近いもの
			entityList.addAll datastoreService.prepare(q).asList(withLimit(remain).offset(0))
		}

		// 最悪、数が足りなくてもそのまま返却
		// TODO: とりあえず今はnum=1固定で取得しているので
		//       これで大丈夫なはず…
		return entityList
	}

	/**
	 * テンプレートを取得する。
	 *
	 * @param title テンプレートの内容
	 * @param userId ユーザID
	 */
	def getTemplate(title, userId) {
		def q = new Query(KeyFactory.createKey(KIND_T, title.SHA1))
		def entity = datastoreService.prepare(q).asSingleEntity()
		if (!entity) {
			entity = new Entity(KIND_T, title.SHA1)
			entity[TITLE] = title
			entity[USER_ID] = userId
			setDefault(entity)
			entity.save()
		}
		setDefault(entity)
		return entity
	}

	/**
	 * テンプレートを更新用に取得する。
	 *
	 * @param title テンプレートの内容
	 * @param userId ユーザID
	 * @param clos 処理を行うクロージャ
	 *             引渡しパラメータ:entity(KIND=KeywordTemplate)
	 */
	def getTemplateForUpdate(title, userId, clos) {
		datastoreService.withTransaction {
			def entity = getTemplate(title, userId)
			clos(entity)
			entity.save()
		}
	}

	/**
	 * ランダムにキーフレーズを取得する。
	 *
	 * @param size 取得するサイズ(0の場合は文字数指定なし)
	 * @param num 取得数(初期値:1)
	 * @return KIND=KeywordKeyPhrase
	 */
	def getRandomKeyPhrase(size, num = 1) {
		def uuid = TextUtil.getUUID()
		def entityList = []

		// とりあえず小さいか同じほうで取得してみる
		// select * from KeywordKeyPhrase
		// where size = 取得するキーフレーズのサイズ and
		//       uuid <= UUID
		// order by uuid desc
		
		def q = new Query(KIND_P)
				.addFilter(_UUID, Query.FilterOperator.LESS_THAN_OR_EQUAL, uuid) // uuidが指定より小さいか同じもの
				.addSort(_UUID, Query.SortDirection.DESCENDING)					 // 指定のuuidに一番近いもの
		if (size) {
			q = q.addFilter(SIZE, Query.FilterOperator.EQUAL, size) // キーフレーズ数
		}
		entityList.addAll datastoreService.prepare(q).asList(withLimit(num).offset(0))

		if (entityList.size() == num) {
			// 指定数分取得できている場合返却
			return entityList
		} else {
			// 指定数分取得できていない場合、大きいほうで取得してみる
			// select * from KeywordKeyPhrase
			// where size = 取得するキーフレーズのサイズ and
			//       uuid > UUID
			// order by uuid asc

			def remain = num - entityList.size()	// 残りいくつ必要か
			q = new Query(KIND_P)
					.addFilter(_UUID, Query.FilterOperator.GREATER_THAN, uuid) // uuidが指定より大きいもの
					.addSort(_UUID, Query.SortDirection.ASCENDING)			   // 指定のuuidに一番近いもの
			if (size) {
				q = q.addFilter(SIZE, Query.FilterOperator.EQUAL, size) // キーフレーズ数
			}
			entityList.addAll datastoreService.prepare(q).asList(withLimit(remain).offset(0))
		}

		// 数が足りなかったら、足りない分は長さ指定無しで取得
		if (entityList.size() < num) {
			return entityList + getRandomKeyPhrase(0, num - entityList.size())
		} else {
			return entityList
		}
	}

	/**
	 * キーフレーズを取得する。
	 *
	 * @param title キーフレーズの内容
	 * @param userId ユーザID
	 */
	def getKeyPhrase(title, userId) {
		def q = new Query(KeyFactory.createKey(KIND_P, title.SHA1))
		def entity = datastoreService.prepare(q).asSingleEntity()
		if (!entity) {
			entity = new Entity(KIND_P, title.SHA1)
			entity[TITLE] = title
			entity[USER_ID] = userId
			setDefault(entity)
			entity.save()
		}
		setDefault(entity)
		return entity
	}

	/**
	 * キーフレーズを更新用に取得する。
	 *
	 * @param title キーフレーズの内容
	 * @param userId ユーザID
	 * @param clos 処理を行うクロージャ
	 *             引渡しパラメータ:entity(KIND=KeywordKeyPhrase)
	 */
	def getKeyPhraseForUpdate(title, userId, clos) {
		datastoreService.withTransaction {
			def entity = getKeyPhrase(title, userId)
			clos(entity)
			entity.save()
		}
	}

	//------------------------------------------------
	// Utilities

	/**
	 * 初期値を設定
	 */
	static def setDefault(def entity) {
		if (!entity[SIZE])
			entity[SIZE] = entity[TITLE].size()
		if (!entity[HASH])
			entity[HASH] = entity.key.name
		if (!entity[_UUID])
			entity[_UUID] = TextUtil.getUUID()
		if (!entity[COUNT_OF_USING])
			entity[COUNT_OF_USING] = 0
		if (!entity[UPDATED_AT])
			entity[UPDATED_AT] = DateUtil.now
		if (!entity[CREATED_AT])
			entity[CREATED_AT] = DateUtil.now
		// テンプレートの場合
		if (entity.kind == KIND_T) {
			if (!entity[KEY_PHRASE_NUM]) {
				entity[KEY_PHRASE_NUM] = KeywordMakerAction.parseSecretMarkList(entity[TITLE]).size()
			}
		}
	}

}
