package dao

import java.util.logging.Logger
import com.google.appengine.api.datastore.*
import static com.google.appengine.api.datastore.FetchOptions.Builder.*

import static util.DaoUtil.*
import hatenahaiku4j.*
import hatenahaiku4j.util.*

/**
 * ステータス情報に関するDAO
 *
 * [kind]
 *   HaikuStatus
 *
 * [field]
 *   util.DaoUtil.STATUS_ID             ステータスID
 *   util.DaoUtil.CREATED_AT            作成日時
 *   util.DaoUtil.FAVORITED             お気に入られ
 *   util.DaoUtil.IN_REPLY_TO_STATUS_ID 返信元ステータスID
 *   util.DaoUtil.IN_REPLY_TO_USER_ID   返信元ユーザID
 *   util.DaoUtil.KEYWORD               キーワード
 *   util.DaoUtil.SOURCE                ソース
 *   util.DaoUtil.TEXT                  投稿内容
 *   util.DaoUtil.USER_ID               ユーザID
 *   util.DaoUtil.FUMOBOT_FLG           ふもぼフラグ
 *   util.DaoUtil.MACALLONI_FLG         まかろにフラグ
 */
class HaikuStatusDao {
	static { ext.Extension } // メソッド拡張
	//------------------------------------------------
	// Constants

	/** このDAOで扱うKIND */
	static final String KIND = 'HaikuStatus'

	//------------------------------------------------
	// Property of this class

	/** データストアサービス */
	DatastoreService datastoreService
	/** ロガー */
	static def log = Logger.getLogger(HaikuStatusDao.class.name)

	//------------------------------------------------
	// Constructor

	/** コンストラクタ */
	HaikuStatusDao() {
		this.datastoreService = DatastoreServiceFactory.getDatastoreService()
	}

	//------------------------------------------------
	// Methods

	/**
	 * ステータスを登録します。
	 * すでに登録されている場合は何もしません。
	 *
	 * @param statusHT hatenahaiku4j.Status
	 */
	def addStatus(hatenahaiku4j.Status status) {
		def entity = getStatusOnlyKey(status.statusId)
		if (!entity) {
			entity = new Entity(KIND, status.statusId)
			// ステータスID
			entity[STATUS_ID]				= status.statusId
			// 作成日時
			entity[CREATED_AT]				= status.createdAt
			// お気に入られ
			entity[FAVORITED]				= status.favorited
			// 返信元ステータスID
			entity[IN_REPLY_TO_STATUS_ID]	= status.inReplyToStatusId
			// 返信元ユーザID
			entity[IN_REPLY_TO_USER_ID]		= status.inReplyToUserId
			// キーワード
			entity[KEYWORD]					= status.keyword
			// ソース
			entity[SOURCE]					= status.source
			// 投稿内容
			entity[TEXT]					= new Text(status.text)
			// ユーザID
			entity[USER_ID]					= status.userId
			// ふもぼフラグ
			entity[FUMOBOT_FLG]				= false
			// まかろにフラグ
			entity[MACALLONI_FLG]			= false
			entity.save()
		}
	}

	/**
	 * ステータスをキーのみで取得する。
	 *
	 * @param statusId ステータスID
	 * @param userId ユーザID
	 */
	def getStatusOnlyKey(statusId) {
		def q = new Query(KeyFactory.createKey(KIND, statusId))
		return datastoreService.prepare(q).asSingleEntity()
	}

	/**
	 * HaikuStatusエンティティのリストを古い順に取得する。
	 * @param flgName フラグ名(初期値:nullならフラグ指定なし)
	 * @return ステータス情報のマップのリスト
	 */
	def getFirstStatus(String flgName = null) {
		def entityList = getStatus(1, flgName)
		entityList ? entityList[0] : null
	}

	/**
	 * HaikuStatusエンティティのリストを古い順に取得する。
	 * @param keyword キーワード
	 * @param flgName フラグ名(初期値:nullならフラグ指定なし)
	 * @return ステータス情報のマップ
	 */
	def getFirstStatusByKeyword(String keyword, String flgName = null) {
		def entityList = getStatusByKeyword(keyword, 1, flgName)
		entityList ? entityList[0] : null
	}

	/**
	 * HaikuStatusエンティティのリストを古い順に取得する。
	 * @param limit 取得上限数
	 * @param flgName フラグ名(初期値:nullならフラグ指定なし)
	 * @return HaikuStatusエンティティのステータス情報のマップのリスト
	 */
	def getStatus(int limit, String flgName = null) {
		def q = new Query(KIND)
			.addSort(CREATED_AT, Query.SortDirection.ASCENDING)	// 最古のもの
		if (flgName) {
			q = q.addFilter(flgName, Query.FilterOperator.EQUAL, false)	// フラグが未処理のもの
		}
		datastoreService.prepare(q).asList(withLimit(limit).offset(0))
	}

	/**
	 * HaikuStatusエンティティのリストを古い順に取得する。
	 * @param keyword キーワード
	 * @param limit 取得上限数
	 * @param flgName フラグ名(初期値:nullならフラグ指定なし)
	 * @return HaikuStatusエンティティの指定したキーワードのステータス情報のマップのリスト
	 */
	def getStatusByKeyword(String keyword, int limit, String flgName = null) {
		def q = new Query(KIND)
			.addFilter(KEYWORD, Query.FilterOperator.EQUAL, keyword)	// キーワードが一致
			.addSort(CREATED_AT, Query.SortDirection.ASCENDING)	// 最古のもの
		if (flgName) {
			q = q.addFilter(flgName, Query.FilterOperator.EQUAL, false)	// フラグが未処理のもの
		}
		datastoreService.prepare(q).asList(withLimit(limit).offset(0))
	}

	/**
	 * HaikuStatusのフラグを立てる
	 * @param statusId ステータスID
	 * @param flgName フラグ名
	 */
	def setFlgOn(statusId, flgName) {
		datastoreService.withTransaction {
			def entity = getStatusOnlyKey(statusId)
			if (entity) {
				entity[flgName] = true
				entity.save()
			}
		}
	}

	/**
	 * HaikuStatusを削除する
	 *
	 * @param statusId ステータスID
	 * @param daysAgo 何日前か
	 * @return 削除に成功した場合true/ でなければfalse
	 */
	def deleteStatusDaysAgo(statusId, daysAgo) {
		def entity = getStatusOnlyKey(statusId)
		if (entity && (DateUtil.now - entity[CREATED_AT]) > daysAgo) {
			return deleteStatus(statusId)
		}
		return false
	}

	/**
	 * HaikuStatusを削除する
	 *
	 * @param statusId ステータスID
	 * @return 削除に成功した場合true/ でなければfalse
	 */
	def deleteStatus(statusId) {
		def result = false
		datastoreService.withTransaction {
			def entity = getStatusOnlyKey(statusId)
			if (entity) {
				entity.delete()
				result = true
			}
		}
		result
	}

	//------------------------------------------------
	// Utilities

}
