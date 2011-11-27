package util

/**
 * テキスト解析など
 */
class TextUtil {

	//------------------------------------------------
	// Constants
	/** 無視パターン */
	static final String IGNORE_PATTERN				= /^[\s　]*?※/
	/** 伏字のパターン */
	static final String SECRET_MARKS_PATTERN		= '○〇◯●×△▲▽▼□■◇◆'.collect{"${it}+"}.join('|')
	/** 伏字のパターン(文字数指定) */
	static final String SECRET_MARKS_SIZED_PATTERN	= '●▲▼■◆'.collect{"${it}+"}.join('|')
	/** キーワードパターン(主にキーワード) */
	static final String KEYWORD_PATTERN				= /【(.+?)】/

	/** キーワードメーカーパターン */
    static final String KEYWORD_MAKER_PATTERN		= /^[\s　]*?※キーワードメーカー※/

	/**
	 * 無視パターンかチェックする。
	 * @param text チェックするテキスト
	 */
	static boolean checkIgnore(text) {
		text =~ /(?s)${IGNORE_PATTERN}/
	}

	/**
	 * 「ありがとう」パターンかチェックする。
	 * @param text チェックするテキスト
	 */
	static boolean checkThanks(text) {
		def reader = new TemplateReader()
		// 外部パターンファイルから「ありがとう」のパターンを取得
		def thanksPatterns = reader.readLines('reply_to_thanks.pattern')
		for(i in 0..<thanksPatterns.size()){
			String pattern = thanksPatterns[i]
			if (text ==~ /${pattern}/) {
				return true
			}
		}
		return false
	}

	/**
	 * 対象が指定されたパターンかチェックする。
	 * @param target チェック対象の文字列
	 * @param pattern チェックパターン
	 */
	static boolean checkPattern(target, pattern) {
		target =~ /(?s)${pattern}/	
	}
	
	/**
	 * UUIDを求める。
	 */
	static String getUUID() {
		java.util.UUID.randomUUID().toString()
	}

}
