import matplotlib.pyplot as plt
from collections import Counter
import pandas as pd
import os
import logging
from preprocessing import load_all_texts, preprocess_amharic_text

def analyze_corpus(documents, output_dir):
    """Analyze corpus and plot Zipf’s law."""
    all_tokens = []
    for tokens in documents.values():
        all_tokens.extend(tokens)

    freq_dict = Counter(all_tokens)

    # Zipf’s law plot (linear scale)
    sorted_freq = sorted(freq_dict.values(), reverse=True)
    ranks = range(1, len(sorted_freq) + 1)
    products = [r * f for r, f in zip(ranks, sorted_freq)]
    avg_product = sum(products[:100]) / min(100, len(products)) if products else 0

    plt.figure(figsize=(10, 6))
    plt.plot(ranks, sorted_freq, label="Rank-Frequency")
    plt.xlabel("Rank")
    plt.ylabel("Frequency")
    plt.title(f"Zipf’s Law Analysis (Avg Rank*Freq: {avg_product:.2f})")
    plt.grid(True)
    plt.legend()
    zipf_plot_path = os.path.join(output_dir, 'zipf_plot.png')
    plt.savefig(zipf_plot_path)
    plt.close()
    logging.info(f"Saved Zipf plot to {zipf_plot_path}")

    return freq_dict

def save_freq_table(filename, freq_dict):
    """Save frequency table as XLSX."""
    if not freq_dict:
        logging.warning("Frequency dictionary is empty. No frequency.xlsx generated.")
        return

    sorted_items = freq_dict.most_common()
    max_freq = sorted_items[0][1]
    data = []
    for rank, (word, freq) in enumerate(sorted_items, 1):
        norm_freq = freq / max_freq
        data.append([word, freq, rank, norm_freq])

    df = pd.DataFrame(data, columns=["Word", "Frequency", "Rank", "Normalized_Frequency"])
    try:
        df.to_excel(filename, index=False)
        logging.info(f"Saved frequency table to {filename}")
    except Exception as e:
        logging.error(f"Error saving frequency table: {e}")

def generate_token_term_summary(documents, index_terms, output_dir, initial_tokens_dict):
    #Generate summary of tokens and indexed terms, using Luhn-filtered terms and precomputed initial tokens.
    os.makedirs(output_dir, exist_ok=True)
    report_file = os.path.join(output_dir, 'token_term_summary.txt')
    
    all_initial_tokens = []
    all_indexed_terms = []
    doc_data = {}
   
    # Process each document using precomputed tokens
    for doc_id, indexed_terms_doc in documents.items():
        initial_tokens = initial_tokens_dict.get(doc_id, [])
        # Filter indexed_terms to include only those in index_terms
        filtered_terms = [term for term in indexed_terms_doc if term in index_terms]
        all_initial_tokens.extend(initial_tokens)
        all_indexed_terms.extend(filtered_terms)
        doc_data[doc_id] = {
            'initial_tokens': initial_tokens,
            'indexed_terms': filtered_terms
        }

    # Write summary report
    try:
        with open(report_file, 'w', encoding='utf-8') as f:
            # System-wide summary
            f.write("System-Wide Summary\n")
            f.write("="*20 + "\n")
            f.write(f"Total Tokens (post-tokenization): {len(all_initial_tokens)}\n")
            f.write(f"Unique Tokens: {len(set(all_initial_tokens))}\n")
            f.write(f"Total Indexed Terms (post-preprocessing and Luhn’s filtering): {len(all_indexed_terms)}\n")
            f.write(f"Unique Indexed Terms: {len(set(all_indexed_terms))}\n")
            f.write("\nTop 5 Tokens:\n")
            for token, count in Counter(all_initial_tokens).most_common(5):
                f.write(f"  {token}: {count}\n")
            f.write("\nTop 5 Indexed Terms:\n")
            for term, count in Counter(all_indexed_terms).most_common(5):
                f.write(f"  {term}: {count}\n")
            f.write("\n" + "="*80 + "\n\n")
            
            # Per-document summary
            for doc_id, data in sorted(doc_data.items()):
                f.write(f"Document: {doc_id}\n")
                f.write("-"*20 + "\n")
                f.write(f"Total Tokens: {len(data['initial_tokens'])}\n")
                f.write(f"Unique Tokens: {len(set(data['initial_tokens']))}\n")
                f.write(f"Total Indexed Terms: {len(data['indexed_terms'])}\n")
                f.write(f"Unique Indexed Terms: {len(set(data['indexed_terms']))}\n")
                f.write("\n" + "="*80 + "\n\n")
        logging.info(f"Saved token term summary to {report_file}")
    except Exception as e:
        logging.error(f"Error writing summary report: {e}")
        