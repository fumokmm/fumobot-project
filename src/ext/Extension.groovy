package ext

/** 拡張クラス */
class Extension {
	static {
		// IntRangeの拡張
		IntRange.metaClass.define {
			/** 範囲内でランダム */
			random {
				int from = delegate.isReverse() ? to : from
				int to   = delegate.isReverse() ? from : to
				int size = to - from + 1
				(Math.floor(Math.random() * size) + from) as int
			}
		}

		// Listの拡張
		List.metaClass.define {
			/** リストをシャッフルした新しいリストを返却 */
			shuffle {
				delegate.clone().with {
					Collections.shuffle(it); it
				}
			}

			/** リストの最大値を指定して取得した新しいリストを返却 */
			atMost { most ->
				delegate[0..<[delegate.size(), most].min()]
			}
		}

		// Stringの拡張
		String.metaClass.define {
			/** 全角や改行や半角スペースを含めて文字列の前後をトリム */
			fullTrim {
				delegate.replaceAll(/(?s)^[\s　]+|[\s　]+$/){''}
			}

			/** 改行も消してフラット化する */
			flatten {
				delegate.split('\n').toList()*.fullTrim().join()
			}

			/** SHA1ハッシュを求める。 */
			getSHA1 {
				def md = java.security.MessageDigest.getInstance('SHA-1')
				md.update(delegate.getBytes('UTF-8'))
				md.digest().collect{ String.format('%02x', it) }.join()
			}
		}
	}
}
