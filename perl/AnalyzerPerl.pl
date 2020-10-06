#!/usr/bin/perl

use strict;
use warnings;

use feature 'say';


my @files = readDirectory();
my %easyWordList = getEasyWordList();
for (@files) {
  if (substr($_, 0, 1) ne ".") {
      analyzeFile("$_", %easyWordList)
  }
}

exit;

sub readDirectory {
  opendir my $dir, "/pub/pounds/CSC330/translations" or die "Cannot open Directory";
  my @files = readdir $dir;
  closedir $dir;
  return @files
}

sub getEasyWordList {
  my @easyWords;
  my %easyWordList;
  my $fileName = "/pub/pounds/CSC330/dalechall/wordlist1995.txt";
  open(my $fh, '<', $fileName) or die $!;
  while(<$fh>) {
    push(@easyWords, split /\s+/);
  }
  close($fh);
  foreach my $str (@easyWords) {
    $easyWordList{$str}++;
  }
  return %easyWordList;
}

sub analyzeFile {
  my ($fileName, %easyWords) = @_;
  my @wordsList = getWordList($fileName);
  my ($numSentences, @words) = getSentencesAndCleanWords(@wordsList);
  my $numSyllables = getNumSyllables(@words);
  my $numDifficult = getNumDifficult(\@words, \%easyWords);
  my $numWords = @words;
  my $flesch = calculateFleschScore($numWords, $numSentences, $numSyllables, $numDifficult);
  my $fleschKincaid = calculateFleschKincaidScore($numWords, $numSentences, $numSyllables, $numDifficult);
  say "$fileName, $numWords, $numSentences, $numSyllables, $numDifficult, $flesch, $fleschKincaid";

}

sub getWordList {
  my $fileName = $_;
  my @wordList;
  my $file = "/pub/pounds/CSC330/translations/".$fileName;
  open(my $fhe, '<', $file) or die $!;
  while(my $row = <$fhe>) {
    chomp $row;
    my @seperate = split ' ', $row;
    foreach my $word (@seperate) {
      if ($word !~ /^[0-9]+$/) {
        push(@wordList, $word);
      }
    }
  }
  close($fhe);
  return @wordList;
}

sub getSentencesAndCleanWords {
  my @wordList = @_;
  my @punct = ['.', ':', ';', '?', '!'];
  my $numSentences = 0;
  my $index = 0;
  for my $word (@wordList) {
    foreach my $char (split //, $word) {
      if ($char ~~ @punct) {
        $numSentences++;
      }
    }
    $word =~ cleanWord($word);
    say $word;
    $index++;
  }

  return $numSentences, @wordList;
}

sub cleanWord {
  my $word = $_;
  $word =~ s/[\W\d_]//g;
  # for my $i (0..length($word)-1) {
  #   my $char = substr($word, $i, 1);
  #   if ($char !~ /[a-zA-z]/) {
  #     $tmp = substr($word, $i, 1, "");
  #   }
  # }
  return $word;
}

sub getNumDifficult {
  my ($wordList, $easyWords) = @_;
  my $numDifficult = 0;
  my %temp = %$easyWords;
  foreach my $word (@$wordList) {
    if (exists($temp{$word})) {
      $numDifficult++;
    }
  }
  return scalar(@$wordList) - $numDifficult;
}

sub getNumSyllables {
  my @wordList = @_;
  my @vowels = ['a','e','i','o','u','y','A','E','I','O','U','Y'];
  my $numSyllables = 0;
  for (@wordList) {
    my $numToAdd = 0;
    for my $i (0..length($_)-1) {
      my $char = substr($_, $i, 1);
      if ($char ~~ @vowels) {
        if ($i == length($_)-1) {
          if ($_[$i] eq 'e' or $_[$i] eq 'E') {
            if (length($_) > 1) {
              my $char2 = substr($_, $i-1, 1);
              if ($char2 eq 'l' or $char2 eq 'L') {
                if (length($_) > 2) {
                  my $char3 = substr($_, $i-2, 1);
                  if ($char3 !~ @vowels) {
                    $numToAdd++;
                  }
                } else {
                  $numToAdd++;
                }
              }
            } else {
              $numToAdd++;
            }
          } else {
            $numToAdd++;
          }
        } elsif (substr($_, $i+1, 1) ~~ @vowels){
          $numToAdd++;
          $i++;
        } else {
          $numToAdd++;
        }
      }
    }
    if ($numToAdd == 0) {
      $numToAdd = 1;
    }
    $numSyllables+=$numToAdd;
  }
  return $numSyllables;
}

sub calculateFleschScore {
  my ($numWords, $numSentences, $numSyllables, $numDifficult) = @_;

  my $alpha = $numSyllables / $numWords;
  my $beta = $numWords / $numSentences;

  my $grade = 206.835 - ($alpha * 84.6) - ($beta * 1.015);
  return $grade;
}

sub calculateFleschKincaidScore {
  my ($numWords, $numSentences, $numSyllables, $numDifficult) = @_;

  my $alpha = $numSyllables / $numWords;
  my $beta = $numWords / $numSentences;

  my $grade = ($alpha * 11.8) + ($beta * 0.39) - 15.59;
  return $grade;
}
