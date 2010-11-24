import com.google.appengine.api.datastore.*

import util.*
import dao.CommandQueueDao
import static util.DaoUtil.*

import hatenahaiku4j.*

result = null
botName = params.bot

try {
	datastoreService.withTransaction{
		def command = new CommandQueueDao().getFirstCommand(botName)
		if (command) {
			switch(command[SERVICE]) {
				case 'haiku' :
					forHaiku(command)
					break
				case 'twitter' :
					forTwitter(command)
					break
			}
			// 投稿完了後にコマンドを削除
			command.delete()
		}
	}
} catch(Exception e) {
	e.printStackTrace()
}

/** ハイク用エントリ */
def forHaiku(def command) {
	def reader = new TemplateReader()
	def botHaikuPass = reader.readUserPassword('password/haiku.txt')
	def version = new TemplateReader().readVersion()

//	def botHaikuSource = [
//		'fumobot'		: "bot/fumobot ${version} powered by HatenaHaiku4J",
//		'macalloni'		: "bot/macalloni ${version} powered by HatenaHaiku4J",
//		'fumoinfoboard'	: "bot/fumoinfoboard ${version} powered by HatenaHaiku4J",
//	]
	def botHaikuSource = [
		'fumobot'		: "bot/fumobot powered by HatenaHaiku4J",
		'macalloni'		: "bot/macalloni powered by HatenaHaiku4J",
		'fumoinfoboard'	: "bot/fumoinfoboard powered by HatenaHaiku4J",
	]
		
	def bot = new LoginUser(botName, botHaikuPass[botName], botHaikuSource[botName])
	def apiAuth = new HatenaHaikuAPI(bot)
	
	// コマンドの種類により処理分け
	try {
		switch(command[COMMAND]) {
			case 'entry' :
				// 二重投稿防止（投稿しようとしてる内容と最終投稿の内容が同じ場合はentry中止）
				if (apiAuth.getKeywordTimeline(command[KEYWORD]).first().text == command[TEXT].value) break
				result = apiAuth.entry(
					command[KEYWORD],
					command[TEXT].value	// Text型なので#getValue()で取得
				)
				break
			case 'reply' :
				// 二重投稿防止（返信元のreplyにすでに自分がいる場合はreply中止）
				if (apiAuth.getStatus(command[IN_REPLY_TO_STATUS_ID]).replies*.userId.contains(botName)) break
				result = apiAuth.reply(
					command[IN_REPLY_TO_STATUS_ID],
					command[TEXT].value	// Text型なので#getValue()で取得
				)
				break
		}
	} catch (HatenaHaikuException e) {
		println 'ハイク投稿で失敗したら何もしない'
	}
}

/** Twitter用エントリ */
def forTwitter(def command) {
	def botTwitterPath = [
		'fumobot'		: 'xxxxx',
		'macalloni'		: 'xxxxx',
	]
	// TODO 実装
}

html.html {
	head {
		title "${botName} entry"
	}
	body {
		h1 "${botName} entry"
		h2(style: 'color:blue') {
			if (result) {
				yield 'success ->'
				a (href: result.link, result.link)
			} else {
				yield 'no entry'
			}
		}
	}
}
