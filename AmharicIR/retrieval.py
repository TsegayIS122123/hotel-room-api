import numpy as np
import os
from scipy.sparse import csr_matrix
import logging
from collections import Counter
import json

def cosine_similarity(vec1, vec2):
    """Compute cosine similarity between sparse vectors."""
    dot_product = vec1.dot(vec2.T).toarray()[0, 0]
    norm1 = np.linalg.norm(vec1.toarray())
    norm2 = np.linalg.norm(vec2.toarray())
    return dot_product / (norm1 * norm2) if norm1 > 0 and norm2 > 0 else 0

def search_documents(query_tokens, documents, vocabulary, postings_lists, output_dir, require_all=False):
    """Search documents using TF-IDF with optional all-term requirement, using pointer-based postings."""
    try:
        # Query vector with normalized TF
        query_vector = Counter(query_tokens)
        query_terms = set(query_tokens)
        max_query_freq = max(query_vector.values()) if query_vector else 1
        query_tf = {term: freq / max_query_freq for term, freq in query_vector.items()}

        # Compute IDF
        N = len(documents)
        idf = {term: np.log(N / vocabulary[term]['DFj']) if term in vocabulary and vocabulary[term]['DFj'] > 0 else 0 for term in query_terms}

        query_tfidf = {term: tf * idf.get(term, 0) for term, tf in query_tf.items()}
        logging.debug(f"Query tokens: {query_tokens}, Query TF-IDF: {query_tfidf}")

        # Compute document TF-IDF and filter
        doc_tf = {}
        doc_max_freq = {}
        for term in query_terms:
            if term in vocabulary:
                postings_index = vocabulary[term]['pointer']
                postings_list = postings_lists[postings_index]
                for doc_id, tf in postings_list:
                    if doc_id not in doc_tf:
                        doc_tf[doc_id] = {}
                    doc_tf[doc_id][term] = tf
                    doc_max_freq[doc_id] = max(doc_max_freq.get(doc_id, 0), tf)

        # Normalize document TF and compute TF-IDF
        doc_tfidf = {}
        for doc_id in doc_tf:
            doc_tfidf[doc_id] = {}
            max_freq = doc_max_freq.get(doc_id, 1)
            for term, tf in doc_tf[doc_id].items():
                doc_tfidf[doc_id][term] = (tf / max_freq) * idf.get(term, 0)

        # Filter documents containing all terms if required
        if require_all:
            relevant_docs = set(doc_tfidf.keys())
            for term in query_terms:
                if term in vocabulary:
                    postings_index = vocabulary[term]['pointer']
                    relevant_docs &= set(doc_id for doc_id, _ in postings_lists[postings_index])
            if not relevant_docs:
                return []
        else:
            relevant_docs = set(doc_tfidf.keys())

        # Cosine similarity
        scores = {}
        for doc_id in relevant_docs:
            tfidf_dict = doc_tfidf.get(doc_id, {})
            dot_product = sum(query_tfidf.get(term, 0) * tfidf for term, tfidf in tfidf_dict.items())
            norm_query = np.sqrt(sum(v * v for v in query_tfidf.values()))
            norm_doc = np.sqrt(sum(v * v for v in tfidf_dict.values()))
            score = dot_product / (norm_query * norm_doc) if norm_query * norm_doc > 0 else 0
            if score > 0:
                scores[doc_id] = score
                logging.debug(f"Doc {doc_id} score: {score}, norms: {norm_query}, {norm_doc}")

        # Rank documents
        ranked_docs = sorted(scores.items(), key=lambda x: x[1], reverse=True)[:10]

        # Save results
        result_file = os.path.join(output_dir, 'search_results.txt')
        with open(result_file, 'w', encoding='utf-8') as f:
            for term in query_terms:
                if term in vocabulary:
                    postings_index = vocabulary[term]['pointer']
                    f.write(f"{term} -> {postings_lists[postings_index]}\n")
            for doc_id, score in ranked_docs:
                doc_path = os.path.join(r"C:\Users\HP\Desktop\AmharicIR\Input", f"{doc_id}.txt")
                snippet = ""
                if os.path.exists(doc_path):
                    with open(doc_path, 'r', encoding='utf-8') as df:
                        content = df.read()
                        for term in query_terms:
                            start_idx = content.find(term) if term in content else -1
                            if start_idx >= 0:
                                snippet = content[max(0, start_idx-20):start_idx+80] + "..." if start_idx >= 0 else content[:100] + "..."
                                break
                        if not snippet:
                            snippet = content[:100] + "..."
                f.write(f"Document: {doc_id}, Score: {score:.4f}, Snippet: {snippet}\n")

        logging.info(f"Saved search results to {result_file}")
        return ranked_docs
    except Exception as e:
        logging.error(f"Error in search: {e}")
        return []