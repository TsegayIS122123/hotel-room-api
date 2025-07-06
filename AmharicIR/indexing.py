import json
import os
from collections import Counter
import logging

def apply_luhn(freq_dict, all_stemmed_tokens, lower=2, upper_percent=0.03, index_dir=None, stopwords=None):
    """Select index terms using Luhn’s method, prioritizing stemmed roots."""
    total_words = len(freq_dict)
    upper_cutoff = int(total_words * upper_percent)
    sorted_items = freq_dict.most_common()

    # Create a frequency dict of stemmed tokens
    stemmed_freq = Counter(all_stemmed_tokens)
    index_terms = set()

    # Select index terms (exclude top 3%, low-frequency, stopwords, and single-char terms)
    for i, (word, freq) in enumerate(sorted_items):
        if freq >= lower and i > upper_cutoff and len(word) >= 2 and word not in (stopwords or set()):
            if word in stemmed_freq or any(stemmed_freq.get(stemmed_word, 0) > 0 for stemmed_word in all_stemmed_tokens if word in stemmed_word):
                index_terms.add(word)

    index_terms = list(index_terms)

    # Save index terms for inspection
    if index_dir and index_terms:
        index_terms_file = os.path.join(index_dir, 'index_terms.txt')
        try:
            with open(index_terms_file, 'w', encoding='utf-8') as f:
                for word in index_terms:
                    f.write(f"{word}\n")
            logging.info(f"Saved index terms to {index_terms_file}")
        except Exception as e:
            logging.error(f"Error saving index terms: {e}")

    return index_terms, set()

def build_inverted_index(documents, index_terms, index_dir):
    """Build inverted index with vocabulary and postings files, without terms in postings."""
    vocabulary = {}
    postings_lists = []
    term_frequencies = {}
    term_to_index = {}  # Map terms to their index in postings_lists

    # Build postings lists and term frequencies
    for doc_id, tokens in documents.items():
        logging.debug(f"Document {doc_id} tokens: {tokens[:10]}...")
        token_counts = Counter(tokens)
        term_frequencies[doc_id] = {term: count for term, count in token_counts.items() if term in index_terms}
        for term, tf in token_counts.items():
            if term in index_terms:
                if term not in term_to_index:
                    term_to_index[term] = len(postings_lists)
                    postings_lists.append([])
                postings_lists[term_to_index[term]].append([doc_id, tf])

    # Build vocabulary with pointers as indices
    for term in index_terms:
        index = term_to_index.get(term, -1)
        if index == -1:
            logging.warning(f"Term {term} in index_terms but not in postings")
            continue
        dfj = len(set(doc_id for doc_id, _ in postings_lists[index]))
        cfj = sum(tf for _, tf in postings_lists[index])
        vocabulary[term] = {'DFj': dfj, 'CFj': cfj, 'pointer': index}

    # Save vocabulary
    vocab_file = os.path.join(index_dir, 'vocabulary.json')
    try:
        with open(vocab_file, 'w', encoding='utf-8') as f:
            json.dump(vocabulary, f, ensure_ascii=False, sort_keys=True)
        logging.info(f"Saved vocabulary to {vocab_file}")
    except Exception as e:
        logging.error(f"Error saving vocabulary: {e}")

    # Save postings as a list
    postings_file = os.path.join(index_dir, 'postings.json')
    try:
        with open(postings_file, 'w', encoding='utf-8') as f:
            json.dump(postings_lists, f, ensure_ascii=False)
        logging.info(f"Saved postings to {postings_file}")
    except Exception as e:
        logging.error(f"Error saving postings: {e}")

    return vocabulary, postings_lists