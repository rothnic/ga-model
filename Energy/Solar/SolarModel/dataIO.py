__author__ = 'Nick'


def readData(fileName):
    data = {}
    with open(fileName) as file:
        for line in file:
            parsLine = line.rstrip()
            parsLine = parsLine.split('=')
            data[parsLine[0]] = parsLine[1]
    return data


def writeData(fileName, dataDict):
    with open(fileName, 'w') as file:
        for key in dataDict:
            file.write(str(key) + "=" + str(dataDict[key]) + "\n")
    file.close()