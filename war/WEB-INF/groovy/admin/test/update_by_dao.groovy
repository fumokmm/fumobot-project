import dao.*
import static util.DaoUtil.*

println "KeywordMakerDaoを使ってupdateします。<br/>"

def makerDao = new KeywordMakerDao()

makerDao.getTemplateForUpdate('×××リレー', 'te') { entity ->
	entity[COUNT_OF_USING]++
}

println "処理終了<br/>"