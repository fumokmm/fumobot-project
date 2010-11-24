ext.Extension // メソッド拡張

// フォワード先
def forwardTo = [
	'/batch/clean_haiku_status.groovy',
]

forward forwardTo.shuffle().first()
//redirect forwardTo.shuffle().first()
