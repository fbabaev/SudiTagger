# SudiTagger

### Authors: Firdavskhon Babaev, Dhanush Balaji 
### Dartmouth CS 10, Winter 2024

## Project Overview

This project builds a Part-of-Speech (POS) tagger using a Hidden Markov Model (HMM). The POS tagger labels each word in a sentence with its grammatical role, such as noun, verb, or adjective. This is an essential step in enabling a digital assistant, like "Sudi," to understand and process natural language.

## How It Works

1. Training the Model: 
   - The model is trained on a labeled dataset where each word in a sentence is tagged with its corresponding part of speech.
   - The training process counts the frequency of transitions between tags (e.g., from noun to verb) and the frequency of words associated with each tag.
   - These frequencies are converted into probabilities, which the model uses to determine the most likely sequence of tags for a given sentence.

2. Viterbi Algorithm:
   - The Viterbi algorithm is used to find the most likely sequence of POS tags for a new sentence.
   - It works by calculating the probabilities of different tag sequences and selecting the one with the highest probability.
   - The algorithm tracks the best paths through the model to determine the correct tags based on the context of the sentence.

3. Tagging Sentences:
   - Once trained, the model can be used to tag new sentences.
   - The input is a sentence, and the output is the same sentence with each word labeled with its corresponding POS tag.

### Input Sentence:
```text
Will you cook the fish                                                                                                                  

### Expected Outcome
Will/MOD you/PRO cook/V the/DET fish/N
