// This is a c++ file

// g++ -Wall -Wextra -c AnalyzeC++.cpp -o AnalyzeC++.o
// gcc AnalyzeC++.cpp -lstdc++ -lm -o AnalyzeC++.o
// ./AnalyzeC++.o

#include <string>
#include <iostream>
#include <fstream>
#include <stdio.h>
#include <dirent.h>
#include <sys/types.h>
#include <vector>
#include <set>
#include <ctype.h>
#include <cmath>
#include <bits/stdc++.h>

using namespace std;

vector<string> readFile();
void analyzeFile(string fileName, set<string> easyWords);
set<string> getEasyWordList();
vector<string> getWordList(string fileName);
double getSentencesAndCleanWords(vector<string>& words);
string cleanWord(string word);
double getNumDifficult(vector<string> words, set<string> easyWords);
double getNumberSyllables(vector<string> words);
int CalculateFleschScore(vector<string> wordList, double numSentences, double numSyllables);
double CalculateFleschKincaidGradeLevel(vector<string> wordList, double numSentences, double numSyllables);
double CalculateDaleChallScore(vector<string> wordList, double numSentences, double numDifficult);

int main() {
	vector<string> files = readFile();
	set<string> easyWordList = getEasyWordList();
	for (int i = 6; i < 7/*files.size()*/; i++) {
		analyzeFile(files[i], easyWordList);
	}
	return 0;
}

vector<string> readFile() {
	vector<string> tmp;
	struct dirent *entry;
	DIR *dir = opendir("/pub/pounds/CSC330/translations");

	while ((entry = readdir(dir)) != NULL) {
		if (isalpha(entry->d_name[0])) {
			tmp.push_back(entry->d_name);
		}
	}
	closedir(dir);
	return tmp;
}

void analyzeFile(string fileName, set<string> easyWords) {
	vector<string> words = getWordList(fileName);
	double numSentences = getSentencesAndCleanWords(words);
	double numSyllables = getNumberSyllables(words);
	double numDifficult = getNumDifficult(words, easyWords);
	int flesch = CalculateFleschScore(words, numSentences, numSyllables);
	double num2 = CalculateFleschKincaidGradeLevel(words, numSentences, numSyllables);
	double num3 = CalculateDaleChallScore(words, numSentences, numDifficult);
	cout << words.size() << ", " << numSentences << ", " << numSyllables << ", " << numDifficult << endl;
	cout << fileName << " - Flesch: " << flesch << ", Flesch-Kincaid: " << num2 << ", DaleChall: " << num3 << endl;
}

set<string> getEasyWordList() {
	set<string> easyWords;
	string word;
	ifstream myFile("/pub/pounds/CSC330/dalechall/wordlist1995.txt");
	while (getline (myFile, word)) {
			easyWords.insert(word);
	}
	myFile.close();
	return easyWords;
}

vector<string> getWordList(string fileName) {
	vector<string> wordList;
	string line;
	ifstream file("/pub/pounds/CSC330/translations/" + fileName);
	while (getline (file, line)) {
			istringstream ss(line);
			do {
				string word;
				ss >> word;
				if (word.find_first_of("0123456789") == string::npos && word != "") {
					wordList.push_back(word);
				}
			} while (ss);
	}
	file.close();
	return wordList;
}

double getSentencesAndCleanWords(vector<string>& words) {
  string punct = ".:;?!";
	double numSentences = 0;
	for (int i = 0; i < words.size(); i++) {
    string tmp = words[i];
		if (punct.find(tmp.back()) < punct.length()) {
			numSentences++;
		}
		words[i] = cleanWord(tmp);
	}
	return numSentences;
}

string cleanWord(string word) {
	for (int i = 0; i < word.length(); i++) {
		if (!isalpha(word[i])) {
			word.erase(i,1);
		}
	}
	return word;
}

double getNumDifficult(vector<string> words, set<string> easyWords) {
	double numDifficult = 0;
	for (string s : words) {
		if (easyWords.find(s) == easyWords.end()) {
			numDifficult++;
		}
	}
	return numDifficult;
}

double getNumberSyllables(vector<string> words) {
	string vowels = "aeiouyAEIOUY";
	double numSyllables = 0;
	for (string word : words) {
		int numToAdd = 0;
		for (int i = 0; i < word.length(); i++) {
			if (vowels.find(word[i]) != std::string::npos) {
				if (i == word.length() - 1) {
					if (word[i] == 'e' || word[i] == 'E') {
						if ((word[i-1] == 'l' || word[i-1] == 'L') && vowels.find(word[i-2]) == std::string::npos) {
							numToAdd++;
						}
					} else {
						numToAdd++;
					}
				} else if (vowels.find(word[i+1]) != std::string::npos) {
					numToAdd++;
					i++;
				} else {
					numToAdd++;
				}
			}
		}
		if (numToAdd == 0) {
			numToAdd = 1;
		}
		numSyllables+=numToAdd;
	}
	return numSyllables;
}

int CalculateFleschScore(vector<string> wordList, double numSentences, double numSyllables) {
	double alpha = numSyllables / (double) wordList.size();
	double beta = (double) wordList.size() / numSentences;

	double index = 206.835 - (alpha * 84.6) - (beta * 1.015);
	return (int) round(index);
}

double CalculateFleschKincaidGradeLevel(vector<string> wordList, double numSentences, double numSyllables) {
	float alpha = numSyllables / (double) wordList.size();
	float beta = (double) wordList.size() / numSentences;

	double grade = (alpha * 11.8) + (beta * 0.39) - 15.59;
	return round(grade * 10.0) / 10.0;
}

double CalculateDaleChallScore(vector<string> wordList, double numSentences, double numDifficult){
	double alpha = numDifficult / (double) wordList.size();
	double beta = (double) wordList.size() / numSentences;

	double diffPercent = alpha * 100;
	double grade = (beta * 0.0496) + (.1579 * diffPercent);
	if (diffPercent > 5)
	{
			grade += 3.6365;
	}
	return round(grade * 10.0) / 10.0;
}
