import com.google.appengine.api.datastore.*
println 'Hello, Low Level API Update<br/>'

Query query = new Query("person")
DatastoreService service = DatastoreServiceFactory.getDatastoreService()