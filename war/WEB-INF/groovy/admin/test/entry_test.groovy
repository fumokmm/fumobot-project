import hatenahaiku4j.*
import hatenahaiku4j.util.*

import util.*

def reader = new TemplateReader()
def text = reader.read(
	'fortune/reply_to_fortune_telling_you_are_welcome.tmpl',
	['userName': 'ふも']
)
def text2 = reader.read(
	'fortune/fortune_telling.tmpl',
	[
		'userName': 'ふも',
		'count': 150,
		'luckey': 'luckey',
		'up': 'up',
		'star': 'star',
		'down': 'down',
		'help': 'help',
	]
)

def apiAuth = new HatenaHaikuAPI(new LoginUser('fumoinfoboard', 'xxxxx'))
apiAuth.entry("""\
Entry ふろむ　GAE
${text}
${text2}
${reader.readVersion()}
""").with{
println "<a href=\"${it.link}\">結果</a>"
}
