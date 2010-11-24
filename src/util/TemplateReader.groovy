package util

import groovy.text.SimpleTemplateEngine

/**
 * テンプレート読み込みクラス
 */
class TemplateReader {
	/** ベースパス */
	String base = 'WEB-INF/template/'
	
	/** テンプレートにバインドして読み込む */
	String read(String fileName, Map binding = null) {
		def engine   = new SimpleTemplateEngine()
		def template = engine.createTemplate(_getReader(fileName))
		if (!binding) binding = [:]
		def message  = template.make(binding)
		message.toString()
	}

	/** テンプレートを行単位で読み込んでリストを返却 */
    List readLines(String fileName) {
        def text = _getFile(fileName).readLines()
        // コメントと空行は削除して返却
        text.findAll{ !(it ==~ /(^\/\/.*$)|(^#.*$)|(^$)/) }
    }
	
	/** バージョン情報を読み込む */
	def readVersion() {
		_getReader('version.txt').readLines().first().tokenize('\t').first()
	}
	
	/** ユーザパスワード情報を読み込んでMapにして返却する */
	def readUserPassword(String fileName) {
		readLines(fileName).inject([:]){ result, item ->
			item.tokenize('=').with{ result[it[0]] = it[1] }
			result
		}
	}

	/** ファイルを取得する */
	File _getFile(fileName = '') {
		new File(this.base + '/' + fileName)
	}

	/** リーダーを取得するbyファイル名 */
	Reader _getReader(fileName = '') {
		new InputStreamReader(new FileInputStream(_getFile(fileName)), 'UTF-8')
	}
}
