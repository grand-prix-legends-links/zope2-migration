@Grab('org.yaml:snakeyaml:1.25')
import groovy.xml.XmlSlurper
import org.yaml.snakeyaml.*

def groupOrder = [
'''Absolute Must Visit Sites''',
'''To Do's if You're New to GPL''',
'''GPL On-line Leagues''',
'''GPL Inactive On-line Leagues''',
'''GPL Off-line Leagues''',
'''GPL Challenges''',
'''French Language Sites''',
'''Sim Racing''',
'''General GPL Sites''',
'''GPL Tracks''',
'''GPL Track Updates''',
'''GPL Trackbuilding''',
'''Carsets - Graphics''',
'''GPL Sounds''',
'''Racing History''',
'''GPL Utilities / Editors''',
'''Replays / Setups''',
'''Technical Stuff''',
'''GPL Driving School''',
'''Track Guides''',
'''Race Reports''',
'''GPL Movie/Photo''',
'''GPL Forums''',
'''Archival''',
]

def known = groupOrder.toSet()

def xml = new XmlSlurper().parse("gpl_ultimate_links.xml")

def pos = xml.record.findAll{ it.pickle[0].global.@name=='PropertyObject' }

def b64decode(x) {
	try {
		new String(Base64.decoder.decode(x), 'UTF-8')
	} catch (e) {
		println "Failed to decode ${x}: ${e.message}"
		x
	}
}

def raw = pos.collect{
	it.pickle[1].dictionary.item.findAll{
		it.key.text() in ["title","comment","url","category"].toSet()
	}.collectEntries{
		[it.key.text(), it.value.string.@encoding=='base64' ? b64decode(it.value.string.text()) : it.value.text() ]
	}
}

def grouped = raw.groupBy{ if (known.contains(it.category)) return it.category; return "Archival" }

def data = groupOrder.findAll{ 
	grouped.containsKey(it) 
}.collect{
	[title: it, links: grouped[it].findAll{
		it.title && it.url
	}.sort{
		it.title
	}.collect{ l ->
		[
			title: l.title,
			link: l.url,
		].tap{
			if (l.comment) {
				comment = l.comment
			}
		}
	}]
}

("links.yml" as File).withWriter{
	def options = new DumperOptions().tap{
		setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK)
	}
	new Yaml(options).dump(data, it)
}
