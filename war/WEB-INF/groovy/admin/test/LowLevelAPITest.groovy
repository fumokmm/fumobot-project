import com.google.appengine.api.datastore.*
println 'Hello, Low Level API<br/><br/>'

Entity entity = new Entity("person")

//マップアクセスのような添字記法
entity['name'] = "ふも"
//普通のプロパティアクセス記法
entity.age = 28

Query query = new Query("person")
DatastoreService service = DatastoreServiceFactory.getDatastoreService()