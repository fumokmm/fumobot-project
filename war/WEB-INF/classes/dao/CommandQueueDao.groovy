package dao

import com.google.appengine.api.datastore.*
import static com.google.appengine.api.datastore.FetchOptions.Builder.*

import static util.DaoUtil.*

/**
 * コマンドキューに関するDAO
 *
 * [kind]
 *   CommandQueue
 *
 * [field]
 *   util.DaoUtil.SERVICE               サービス[haiku or twitter]
 *   util.DaoUtil.CREATED_AT            作成日時
 *   util.DaoUtil.WHO                   誰宛
 *   util.DaoUtil.COMMAND               コマンドの種類
 *   util.DaoUtil.IN_REPLY_TO_STATUS_ID 返信元ステータスID
 *   util.DaoUtil.KEYWORD               キーワード
 *   util.DaoUtil.TEXT                  投稿内容
 *   util.DaoUtil.PRIORITY              優先度
 */
class CommandQueueDao {

	//------------------------------------------------
	// Constants

	/** このDAOで扱うKIND */
	static final String KIND = 'CommandQueue'

	//------------------------------------------------
	// Property of this class

	/** データストアサービス */
	DatastoreService datastoreService

	//------------------------------------------------
	// Constructor

	/** コンストラクタ */
	CommandQueueDao() {
		this.datastoreService = DatastoreServiceFactory.getDatastoreService()
	}

	//------------------------------------------------
	// Methods

	/**
	 * コマンドを追加する。
	 * @param param コマンドが設定されたMap
	 */
	def addCommand(def param) {
		def command = new Entity(KIND)

		// サービス[haiku or twitter]
		if (param[SERVICE])
			command[SERVICE]			= new String(param.service)
		// 作成日時
		if (param[CREATED_AT])
			command[CREATED_AT]			= param.createdAt
		// 誰宛
		if (param[WHO])
			command[WHO]				= new String(param.who)
		// コマンドの種類
		if (param[COMMAND])
			command[COMMAND]			= new String(param.command)
		// 返信元ステータスID
		if (param[IN_REPLY_TO_STATUS_ID])
			command[IN_REPLY_TO_STATUS_ID]	= new String(param.inReplyToStatusId)
		// キーワード
		if (param[KEYWORD])
			command[KEYWORD]			= new String(param.keyword)
		// 投稿内容
		if (param[TEXT])
			command[TEXT]				= new Text(param.text)
		// 優先度
		if (param[PRIORITY])
			command[PRIORITY]			= param.priority
		else
			command[PRIORITY]			= 999

		// コマンド登録
		command.save()
	}

	/**
	 * 最初のコマンドを取得
	 * @param who ボット名(userId)
	 */
	def getFirstCommand(String who) {
		def q = new Query(KIND)
			.addFilter(WHO, Query.FilterOperator.EQUAL, who)	// 誰に対する命令？
			.addSort(PRIORITY, Query.SortDirection.ASCENDING)	// 優先度が高いもの(1:最高)
			.addSort(CREATED_AT, Query.SortDirection.ASCENDING)	// 最古のもの

		datastoreService.prepare(q).asList(withLimit(1).offset(0)).with {
			it ? it.first() : null
		}
	}

	//------------------------------------------------
	// Utilities

}
