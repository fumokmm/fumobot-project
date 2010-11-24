import com.google.appengine.api.datastore.*
import static com.google.appengine.api.datastore.FetchOptions.Builder.*
import hatenahaiku4j.util.*

def now = DateUtil.now

def userInfo = new Entity('UserInfo', 'fumokmm')
userInfo.userName = new String('ふも２')
userInfo.countOfKeywordUranai = 200
userInfo.updatedAt = now

// コマンド登録
datastoreService.withTransaction{
	userInfo.save()
}

def q = new Query(KeyFactory.createKey('UserInfo', 'fumokmm2'))
//	.addFilter(Entity.KEY_RESERVED_PROPERTY, Query.FilterOperator.EQUAL, 'fumokmm')
//	.setKeysOnly()

def result = datastoreService.prepare(q)
	.asSingleEntity()
//	.asList(withLimit(1).offset(0))
println result
if (result) {
	println "${result.key.name} ${result.userName} ${result.countOfKeywordUranai} ${result.updatedAt}"
} else {
	println 'no result'
}

