package util

/**
 * テキスト解析など
 */
class DaoUtil {

	//------------------------------------------------
	// Constants

	//------------------------------------------------
	// Key of field command queue

	/** サービス[haiku or twitter] */
	static final String SERVICE					= 'service'
	/** 誰宛  */
	static final String WHO						= 'who'
	/** コマンドの種類 */
	static final String COMMAND					= 'command'
	/** 優先度 */
	static final String PRIORITY				= 'priority'

	//------------------------------------------------
	// Key of field for HaikuStatus
	
	/** ステータスID */
	static final String STATUS_ID				= 'statusId'
	/** お気に入られ */
	static final String FAVORITED				= 'favorited'
	/** 返信元ステータスID */
	static final String IN_REPLY_TO_STATUS_ID	= 'inReplyToStatusId'
	/** 返信元ユーザID */
	static final String IN_REPLY_TO_USER_ID		= 'inReplyToUserId'
	/** キーワード */
	static final String KEYWORD					= 'keyword'
	/** ソース */
	static final String SOURCE					= 'source'
	/** 投稿内容 */
	static final String TEXT					= 'text'
	/** ユーザID */
	static final String USER_ID					= 'userId'

	/** ふもぼフラグ */
	static final String FUMOBOT_FLG				= 'fumobotFlg'
	/** まかろにフラグ */
	static final String MACALLONI_FLG			= 'macalloniFlg'

	//------------------------------------------------
	// Key of field for CrawlInfo

	/** 次に使うSINCE */
	static final String SINCE					= 'since'
	/** 次にクロールするPAGE */
	static final String PAGE					= 'page'

	//------------------------------------------------
	// Key of field for UserInfo

	/** キーワード占いの回数 */
	static final String COUNT_OF_KEYWORD_URANAI = 'countOfKeywordUranai'
	/** キーワードメーカーの回数 */
	static final String COUNT_OF_KEYWORD_MAKER  = 'countOfKeywordMaker'

	//------------------------------------------------
	// Key of field for KeywordInfo
	
	/** 投稿数 */
	static final String ENTRY_COUNT				= 'entryCount'

	//------------------------------------------------
	// Key of field for KeywordsCount

	/** 最終ページ */
	static final String LAST_PAGE				= 'lastPage'
	/** 最終ページ（携帯） */
	static final String LAST_PAGE_MOBILE		= 'lastPageMobile'
	/** 投稿数 */
	static final String COUNT					= 'count'

	//------------------------------------------------
	// Key of field for KeywordTemplate and KeywordKeyPhrase
	/** タイトル */
	static final String TITLE					= 'title'
	/** タイトルのサイズ(文字数) */
	static final String SIZE					= 'size'
	/** タイトルのハッシュ */
	static final String HASH					= 'hash'
	/** ユニークなID(アクセス時に更新) */
	static final String _UUID					= 'uuid'
	/** フレーズ数 */
	static final String KEY_PHRASE_NUM			= 'keyPhraseNum'
	/** 使用回数 */
	static final String COUNT_OF_USING			= 'countOfUsing'

	//------------------------------------------------
	// Key of field for Grobal
	
	/** 作成日時 */
	static final String CREATED_AT				= 'createdAt'
	/** 更新日時 */
	static final String UPDATED_AT				= 'updatedAt'
		
	/**
	 * データストアから取得されたデータを元に
	 * mapを構築して返却する。
	 * @param data データストアから取得したステータス
	 */
	static def toStatus(data) {
		def status = [:]

		// 元のデータ
		status.data = data

		// ステータスID
		status.statusId = data[STATUS_ID]
		// 作成日時
		status.createdAt = data[CREATED_AT]
		// お気に入られ
		status.favorited = data[FAVORITED]
		// 返信元ステータスID
		status.inReplyToStatusId = data[IN_REPLY_TO_STATUS_ID]
		// 返信元ユーザID
		status.inReplyToUserId = data[IN_REPLY_TO_USER_ID]
		// キーワード
		status.keyword = data[KEYWORD]
		// ソース
		status.source = data[SOURCE]
		// 投稿内容
		status.text = data[TEXT].value	// Text型なので#getValue()で取得
		// ユーザID
		status.userId = data[USER_ID]
		
		// ふもぼフラグ
		status.fumobotFlg = data[FUMOBOT_FLG]
		
		status
	}

	static def toCrawlInfo(data) {
		def info = [:]
		
		// 元のデータ
		info.data = data
		
		// 次に使うSINCE
		info.since = data[SINCE]
		// 次にクロールするPAGE
		info.page = data[PAGE] as int // ※←注意！datastoreから取得した際、datastore上の型がintだがLongで取得された。
		// 更新日時
		info.updatedAt = data[UPDATED_AT]

		info
	}

}
