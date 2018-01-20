# change remote.iml
# from
# <orderEntry type="library" exported="" scope="PROVIDED" name="transaction-api-1.1" level="project" />
# to
# <orderEntry type="library" exported="" scope="COMPILE" name="transaction-api-1.1" level="project" />
# for each dependency

import xml.etree.ElementTree

path = "remote/remote.iml"
tree = xml.etree.ElementTree.parse(path)
for orderEntry in tree.getroot().iter('orderEntry'):
    if 'scope' in orderEntry.attrib:
        orderEntry.attrib['scope'] = 'COMPILE'

tree.write(path)