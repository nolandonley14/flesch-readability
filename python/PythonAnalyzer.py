#This is a python file
#!/usr/bin/python

# python PythonAnalyzer.py

from os import listdir
from os.path import isfile, join

def main():
    onlyfiles = [f for f in listdir("/pub/pounds/CSC330/translations") if isfile(join("/pub/pounds/CSC330/translations", f))]
    easyWords = getEasyWordList()
    for file in onlyfiles:
        analyzeFile(file, easyWords)

def getEasyWordList():
    file = open("/pub/pounds/CSC330/dalechall/wordlist1995.txt", 'r')
    easyWords = set(line.strip() for line in file)
    return easyWords

def analyzeFile(fileName, easyWordList):
    words = getWordList(fileName)
    numSentences = getSentencesAndCleanWords(words)
    numSyllables = getNumSyllables(words)
    numDifficult = getNumDifficult(words, easyWordList)
    flesch = calculateFleschScore(words, numSentences, numSyllables)
    fleschKincaid = calculateFleschKincaidScore(words, numSentences, numSyllables)
    daleChall = calculateDallChallScore(words, numSentences, numDifficult)
    print(fileName + "- Flesch: " + str(flesch) + ", Flesch-Kincaid: " + str(fleschKincaid) + ", DaleChall: " + str(daleChall))

def getWordList(fileName):
    wordList = []
    file = open("/pub/pounds/CSC330/translations/" + fileName)
    lines = file.readlines()
    for line in lines:
        words = line.split()
        for word in words:
            if word != "" and not any(char.isdigit() for char in word):
                wordList.append(word)
    return wordList

def getSentencesAndCleanWords(wordList):
    numSentences = 0
    punctuation = ['.', ':', ';', '?', '!']
    for word in wordList:
        if [punct for punct in punctuation if(punct in word)]:
            numSentences+=1
        word = cleanWord(word)
    return numSentences

def cleanWord(word):
    for ch in word:
        if not ch.isalpha():
            word.replace(ch, '')


def getNumSyllables(wordList):
    vowels = ['a', 'e', 'i', 'o', 'u', 'y', 'A', 'E', 'I', 'O', 'U', 'Y']
    numSyllables = 0
    for word in wordList:
        numToAdd = 0
        for i in range(0, len(word), 1):
            if word[i] in vowels:
                if i == len(word) - 1:
                    if (word[i] == 'e' or word[i] == 'E'):
                        if ((word[i-1] == 'l' or word[i-1] == 'L') and (word[i-2] not in vowels)):
                            numToAdd+=1
                    else:
                        numToAdd+=1
                elif word[i+1] in vowels:
                    i+=1
                else:
                    numToAdd+=1
        if numToAdd == 0:
            numToAdd = 1
        numSyllables+=numToAdd

    return numSyllables

def getNumDifficult(wordList, easyWordList):
        numEasy = 0
        for word in wordList:
            if word in easyWordList:
                numEasy+=1
        return len(wordList) - numEasy

def calculateFleschScore(wordList, sentences, syllables):
    alpha = syllables / float(len(wordList))
    beta = float(len(wordList)) / sentences

    index = 206.835 - (alpha * 84.6) - (beta * 1.015)
    return int(index)

def calculateFleschKincaidScore(wordList, sentences, syllables):
    alpha = syllables / float(len(wordList))
    beta = float(len(wordList)) / sentences

    grade = (alpha * 11.8) + (beta * 0.39) - 15.59
    return round(grade, 1)

def calculateDallChallScore(wordList, sentences, difficult):
	alpha = difficult / float(len(wordList))
	beta = float(len(wordList)) / sentences

	diffPercent = alpha * 100;
	grade = (beta * 0.0496) + (.1579 * diffPercent)
	if diffPercent > 5:
		grade += 3.6365

	return round(grade, 1)

if __name__ == "__main__":
    main();
