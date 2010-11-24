import util.*
import dao.*
import static dao.CommandQueueDao.*

println "${TextUtil.checkIgnore('''
　　　※
無視ぱたぁん

''')}<br/>"

println "${TextUtil.checkThanks('''
　　※
ありがとう

''')}<br/>"

def dao = new CommandQueueDao()
def command = dao.getFirstCommand('fumobot')
if (command) {
	println "${command[TEXT].value}<br/>"
	println "${command[TEXT].value.class}<br/>"
	println "${TextUtil.checkThanks(command[TEXT].value)}<br/>"

}

println "${TextUtil.getSHA1('a')}<br/>"
println "${TextUtil.getUUID()}"
