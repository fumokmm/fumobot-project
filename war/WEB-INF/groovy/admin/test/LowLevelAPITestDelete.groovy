import com.google.appengine.api.datastore.*
println 'Hello, Low Level API Delete<br/>'

Query query = new Query("person")
DatastoreService service = DatastoreServiceFactory.getDatastoreService()