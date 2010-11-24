import com.google.appengine.api.datastore.*import static com.google.appengine.api.datastore.FetchOptions.Builder.*
println 'Hello, Low Level API Delete<br/>'

Query query = new Query("person")
DatastoreService service = DatastoreServiceFactory.getDatastoreService()service.withTransaction{	def persons = service.prepare(query).asList(withOffset(0).limit(1))	println "取得結果${persons.size()}<br/>"	if (persons) {		persons.first().delete()	}}def persons = service.prepare(query).asList(FetchOptions.Builder.withOffset(0))println "----------------------(${persons.size()})<br/>"persons.each {	println "${it.__key__} ${it.name}さん(${it.age})<br/>"}
