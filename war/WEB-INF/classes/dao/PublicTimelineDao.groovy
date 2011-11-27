package dao

import com.google.appengine.api.datastore.*
import static com.google.appengine.api.datastore.FetchOptions.Builder.*

import static util.DaoUtil.*

/**
 * パブリックタイムライン情報に関するDAO
 *
 * [kind]
 *   PublicTimeline
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
class PublicTimelineDao {

	//------------------------------------------------
	// Constants

	/** このDAOで扱うKIND */
	static final String KIND = 'PublicTimeline'

	//------------------------------------------------
	// Property of this class

	/** データストアサービス */
	DatastoreService datastoreService

	//------------------------------------------------
	// Constructor

	/** コンストラクタ */
	PublicTimelineDao() {
		this.datastoreService = DatastoreServiceFactory.getDatastoreService()
	}

	//------------------------------------------------
	// Methods

	/**
	 * ステータスを登録します。
	 * @param statusHT hatenahaiku4j.Status
	 */
	def addStatus(def statusHT) {
		// キー用にステータスIDを取得
		String statusId = new String(statusHT.statusId)

		def status = null
		try {
			// 取得を試みる
			status = datastoreService.get(KeyFactory.createKey(KIND, statusId))
			println "[${statusId}] retrieve success -> ${status}"
			
		} catch(e) {
			// まだない場合新規作成
			status = new Entity(KIND, statusId)
			println "[${statusId}] retrieve failure and make new! -> ${status}"
			// ふもぼフラグ
			status[FUMOBOT_FLG]		= false
			// まかろにフラグ
			status[MACALLONI_FLG]	= false
		}

		// ステータスID
		status[STATUS_ID]				= statusId
		// 作成日時
		status[CREATED_AT]				= statusHT.createdAt
		// お気に入られ
		status[FAVORITED]				= statusHT.favorited
		// 返信元ステータスID
		status[IN_REPLY_TO_STATUS_ID]	= new String(statusHT.inReplyToStatusId)
		// 返信元ユーザID
		status[IN_REPLY_TO_USER_ID]		= new String(statusHT.inReplyToUserId)
		// キーワード
		status[KEYWORD]					= new String(statusHT.keyword)
		// ソース
		status[SOURCE]					= new String(statusHT.source)
		// 投稿内容
		status[TEXT]					= new Text(statusHT.text)
		// ユーザID
		status[USER_ID]					= new String(statusHT.userId)

		// ステータス登録
		status.save()
	}

	/**
	 * PublicTimelineDaoエンティティのリストを古い順に取得する。
	 * @return ステータス情報のマップのリスト
	 */
	def getFirstStatus() {
		def status = getStatus(1)
		status ? status[0] : null
	}

	/**
	 * PublicTimelineDaoエンティティのリストを古い順に取得する。
	 * @param keyword キーワード
	 * @return ステータス情報のマップ
	 */
	def getFirstStatusByKeyword(String keyword) {
		def status = getStatusByKeyword(keyword, 1)
		status ? status[0] : null
	}

	/**
	 * PublicTimelineDaoエンティティのリストを古い順に取得する。
	 * @param limit 取得上限数
	 * @return PublicTimelineのステータス情報のマップのリスト
	 */
	def getStatus(int limit) {
		def q = new Query(KIND)
			.addFilter(FUMOBOT_FLG, Query.FilterOperator.EQUAL, false)	// ふもぼが未処理のもの
			.addSort(CREATED_AT, Query.SortDirection.ASCENDING)	// 最古のもの

		def list = datastoreService.prepare(q).asList(withLimit(limit).offset(0))
		list.collect{ toStatus(it) }
	}

	/**
	 * PublicTimelineDaoエンティティのリストを古い順に取得する。
	 * @param keyword キーワード
	 * @param limit 取得上限数
	 * @return PublicTimelineの指定したキーワードのステータス情報のマップのリスト
	 */
	def getStatusByKeyword(String keyword, int limit) {
		def q = new Query(KIND)
			.addFilter(KEYWORD, Query.FilterOperator.EQUAL, keyword)	// キーワードが一致
			.addFilter(FUMOBOT_FLG, Query.FilterOperator.EQUAL, false)	// ふもぼが未処理のもの
			.addSort(CREATED_AT, Query.SortDirection.ASCENDING)	// 最古のもの

		def list = datastoreService.prepare(q).asList(withLimit(limit).offset(0))
		list.collect{ toStatus(it) }
	}

	//------------------------------------------------
	// Utilities

}
