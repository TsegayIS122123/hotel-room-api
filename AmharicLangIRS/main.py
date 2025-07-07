import os
import logging
from preprocessing import load_all_texts, preprocess_amharic_text, load_stopwords
from indexing import apply_luhn, build_inverted_index
from retrieval import search_documents
from analysis import analyze_corpus, save_freq_table ,generate_token_term_summary

# Configure logging
logging.basicConfig(level=logging.INFO)

def main():
    # Define file paths and directories
    input_dir = r"C:\Users\HP\Desktop\AmharicIR\Input"
    index_dir = r"C:\Users\HP\Desktop\AmharicIR\Index"
    output_dir = r"C:\Users\HP\Desktop\AmharicIR\Output"

    os.makedirs(index_dir, exist_ok=True)
    os.makedirs(output_dir, exist_ok=True)

    # Load stopwords
    stopword_file = r"C:\Users\HP\Desktop\AmharicIR\stopwords.txt"
    stopwords = load_stopwords(stopword_file)

    # Load and preprocess documents
    documents = {}
    initial_tokens_dict = {}  # Store initial tokens for each document
    files = [os.path.join(input_dir, f"AmharicBook{i}.txt") for i in range(1, 18)]
    for file in files:
        if os.path.exists(file):
            text = load_all_texts(file)
            if text:
                initial_tokens, indexed_terms = preprocess_amharic_text(text)
                if indexed_terms:
                    doc_id = os.path.splitext(os.path.basename(file))[0]
                    documents[doc_id] = indexed_terms
                    initial_tokens_dict[doc_id] = initial_tokens  # Save initial tokens
        else:
            logging.warning(f"File not found: {file}")

    if not documents:
        logging.error("No documents processed. Skipping analysis.")
        return

    logging.info(f"Processed {len(documents)} documents")

    # Analyze corpus and build frequency dictionary
    freq_dict = analyze_corpus(documents, output_dir) or {}
    save_freq_table(os.path.join(output_dir, 'frequency.xlsx'), freq_dict)

    # Apply Luhn's method to select index terms
    all_stemmed_tokens = [token for tokens in documents.values() for token in tokens]
    index_terms, _ = apply_luhn(freq_dict, all_stemmed_tokens, index_dir=index_dir, stopwords=stopwords)

    # Generate token and term summary with Luhn-filtered terms
    generate_token_term_summary(documents, index_terms, output_dir, initial_tokens_dict)

    # Build inverted index
    vocabulary, postings_lists = build_inverted_index(documents, index_terms, index_dir)

    # Define search function
    def search_with_index(query):
        query_tokens = preprocess_amharic_text(query)[1]  # Use indexed_terms from query
        return search_documents(query_tokens, documents, vocabulary, postings_lists, output_dir)

    # Create GUI
    from gui import create_gui
    create_gui(search_with_index)

if __name__ == "__main__":
    main()