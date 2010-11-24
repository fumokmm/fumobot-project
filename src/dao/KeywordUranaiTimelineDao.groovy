package dao

import com.google.appengine.api.datastore.*
import static com.google.appengine.api.datastore.FetchOptions.Builder.*

import static util.DaoUtil.*

// TODO PublicTimelineDaoに吸収される予定

/**
 * 「キーワード占い」タイムライン情報に関するDAO
 */
class KeywordUranaiTimelineDao {

	//------------------------------------------------
	// Constants

	/** このDAOで扱うKIND */
	static final String KIND = 'KeywordUranaiTimeline'

	//------------------------------------------------
	// Property of this class

	/** データストアサービス */
	DatastoreService datastoreService

	//------------------------------------------------
	// Constructor

	/** コンストラクタ */
	KeywordUranaiTimelineDao() {
		this.datastoreService = DatastoreServiceFactory.getDatastoreService()
	}

	//------------------------------------------------
	// Methods

	/**
	 * ステータスを登録します。
	 * @param statusHT hatenahaiku4j.Status
	 */
	def addStatus(def statusHT) {
		def status = new Entity(KIND)

		// ステータスID
		status[STATUS_ID]				= new String(statusHT.statusId)
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
	 * TimelineKeywordUranaiエンティティのリストを古い順に取得する。
	 * @param limit 取得上限数
	 * @return Entity TimelineKeywordUranaiエンティティのリスト
	 */
	def getFirstStatus() {
		getStatus(1)[0]
	}

	/**
	 * TimelineKeywordUranaiエンティティのリストを古い順に取得する。
	 * @param limit 取得上限数
	 * @return List<Entity> TimelineKeywordUranaiエンティティのリスト
	 */
	def getStatus(int limit) {
		def q = new Query(KIND)
			.addSort(CREATED_AT, Query.SortDirection.ASCENDING)	// 最古のもの

		datastoreService.prepare(q).asList(withLimit(limit).offset(0))
	}

	//------------------------------------------------
	// Utilities

}
