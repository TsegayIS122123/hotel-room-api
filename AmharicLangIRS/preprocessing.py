import re
import os
from bs4 import BeautifulSoup
import chardet
import logging
from collections import Counter

def load_all_texts(file_path):
    """Load text file with encoding detection."""
    try:
        with open(file_path, 'rb') as f:
            raw_data = f.read()
        result = chardet.detect(raw_data)
        encoding = result['encoding'] if result['confidence'] > 0.7 else 'utf-8'
        logging.info(f"Detected encoding for {file_path}: {encoding} (confidence: {result['confidence']})")
        return raw_data.decode(encoding, errors='replace')
    except Exception as e:
        logging.error(f"Error loading {file_path}: {e}")
        return ""

def load_stopwords(stopword_file):
    """Load stopwords from file."""
    try:
        with open(stopword_file, 'r', encoding='utf-8') as f:
            stopwords = set(line.strip() for line in f if line.strip())
            return stopwords
    except Exception as e:
        logging.error(f"Error loading stopwords: {e}")
        return set()

def preprocess_amharic_text(text, doc_id=None, output_dir=None):
    """Preprocess Amharic text, returning initial tokens and indexed terms."""
    # Load static stop words
    stopword_file = r"C:\Users\HP\Desktop\AmharicIR\stopwords.txt"
    stopwords = load_stopwords(stopword_file)

    # Step 1: Markup Removal
    soup = BeautifulSoup(text, 'html.parser')
    text = soup.get_text()

    # Step 2: Phrase Detection
    phrases = {
        'አዲስ አበባ': 'አዲስ_አበባ',
        'መጽሐፍ ቤት': 'መጽሐፍ_ቤት',
        'ኮቪድ 19': 'ኮቪድ19',
        '510 B.C.': '510_B_C',
        'ዩ.ኤስ.ኤ': 'ዩኤስኤ'
    
    }
    for phrase, joined in phrases.items():
        text = text.replace(phrase, joined)
    logging.debug(f"Text after phrase detection: {len(text)}")

    # Step 3: Normalization
    # Remove non-Amharic characters (keep U+1200–U+137F, whitespace, underscores for phrases)
    text = re.sub(r'[^\u1200-\u137F\s_]', '', text)

    # Punctuation normalization (Ethiopic and Latin)
    punctuation = {
        '፠': ' ', '፡': ' ', '።': ' ', '፣': ' ', '፤': ' ', '፥': ' ', '፦': ' ', '፧': ' ',
        ',': ' ', '.': ' ', ':': ' ', ';': ' ', '!': ' ', '?': ' ', '(': ' ', ')': ' ',
        '[': ' ', ']': ' ', '{': ' ', '}': ' ', '"': ' ', "'": ' '
    }
    # Exceptions for meaningful punctuation (e.g., program names)
    exceptions = ['ዶትኮም']  # e.g., dot.com
    for exc in exceptions:
        text = text.replace(exc, exc.replace('.', '_'))  # Preserve in a safe form
    for punc, replacement in punctuation.items():
        text = text.replace(punc, replacement)

    # Normalize Ethiopic digits
    eth_digits = {'፩': ' ', '፪': ' ', '፫': ' ', '፬': ' ', '፭': ' ', '፮': ' ', '፯': ' ', '፰': ' ', '፱': ' ', '፲': ' '}
    for digit, replacement in eth_digits.items():
        text = text.replace(digit, replacement)
    # Normalize Latin digits (remove unless part of exceptions like ኮቪድ19)
    text = re.sub(r'\b\d+\b', ' ', text)
    # Collapse multiple spaces
    text = re.sub(r'\s+', ' ', text).strip()

    # Hyphen handling
    hyphen_exceptions = ['ኮቪድ19', 'ኤምኤስ_ዶስ']  # e.g., COVID-19, MS-DOS
    for exc in hyphen_exceptions:
        text = text.replace(exc, exc.replace('-', '_'))  # Preserve hyphenated terms
    text = text.replace('-', ' ')  # Split other hyphens
    
    thesaurus = {
        'ተሽከርካሪ': {
            'UF': ['አውቶሞቢል', 'መኪና', 'ተሽከርካሪ ሞተር', 'ተሽከርካሪ መንገድ'],
            'BT': ['መጓጓዣ'],
            'NT': ['መኪና', 'መኪና ጭነት', 'አውቶቡስ', 'ሞተር ሳይክል'],
            'RT': ['መንገድ ትራንስፖርት', 'መንገድ ምህንድስና', 'ቤንዚን', 'ጋራጅ']
        },
        'መኪና': {
            'UF': ['አውቶሞቢል', 'መኪና ተሳፋሪ', 'ሴዳን'],
            'BT': ['ተሽከርካሪ'],
            'NT': ['ታክሲ', 'ስፖርት መኪና', 'ኤስ ዩ ቪ'],
            'RT': ['ሹፌር', 'መንገድ', 'የመኪና ኢንሹራንስ']
        },
        'መኪና ጭነት': {
            'UF': ['ትራክ', 'መኪና ሸክም'],
            'BT': ['ተሽከርካሪ'],
            'NT': ['ትራክተር', 'የጭነት መኪና ትልቅ'],
            'RT': ['ሎጂስቲክስ', 'መጋዘን']
        },
        'መጓጓዣ': {
            'UF': ['ትራንስፖርት'],
            'BT': [],
            'NT': ['ተሽከርካሪ', 'ባቡር', 'አውሮፕላን'],
            'RT': ['መንገድ', 'አውሮፕላን ማረፊያ']
        },
        'መንገድ ትራንስፖርት': {
            'UF': ['የመንገድ መጓጓዣ'],
            'BT': ['መጓጓዣ'],
            'NT': [],
            'RT': ['ተሽከርካሪ', 'መንገድ ምህንድስና']
        }
    }
       #Thesaurus Normalization
    for term, details in thesaurus.items():
        for synonym in details['UF']:
            text = text.replace(synonym, term)


    # Character normalization
    normalized_pairs = list(zip(
        ['ዐ', 'ዑ', 'ዒ', 'ዓ', 'ዔ', 'ዕ', 'ዖ', 'ሀ', 'ሁ', 'ሂ', 'ሃ', 'ሄ', 'ህ', 'ሆ', 'ሰ', 'ሱ', 'ሲ', 'ሳ', 'ሴ', 'ስ', 'ሶ', 'ጸ', 'ጹ', 'ጺ', 'ጻ', 'ጼ', 'ጽ', 'ጾ'],
        ['አ', 'ኡ', 'ኢ', 'ኣ', 'ኤ', 'እ', 'ኦ', 'ሐ', 'ሑ', 'ሒ', 'ሓ', 'ሔ', 'ሕ', 'ሖ', 'ሠ', 'ሡ', 'ሢ', 'ሣ', 'ሤ', 'ሥ', 'ሦ', 'ፀ', 'ፁ', 'ፂ', 'ፃ', 'ፄ', 'ፅ', 'ፆ']
    ))
    for char1, char2 in normalized_pairs:
        text = text.replace(char1, char2)

    # Word-level variants
    variants = {'ቤዯት': 'ቤት', 'ላንተ': 'ለአንተ', 'ላንቺ': 'ለአኩቺ', 'ለን': 'ለኔ', 'እንዴ': 'እኩዴ', 'ወዯዘሪት': 'ወዯሮ', 'መጽሀፍ': 'መጽሐፍ', 'አንቺ': 'አኩች', 'ለካ': 'ለአኩተ', 'ቤትን': 'ቤት', 'ያለ': 'እለ', 'አምራች': 'አምራች', 'እኔን': 'እኔ', 'ወዯም': 'ወዯም'}
    for variant, standard in variants.items():
        text = text.replace(variant, standard)
    logging.debug(f"Text after normalization: {len(text)}")

    # Step 4: Tokenization
    initial_tokens = text.split()

    # Step 5: Filter Non-Amharic Tokens
    amharic_tokens = [token for token in initial_tokens if all(ord(c) in range(0x1200, 0x1380) or c == '_' for c in token)]

    # Step 6: Stopword Removal
    tokens = [token for token in amharic_tokens if token not in stopwords]

    # Step 7: Enhanced Stemming
    def simple_amharic_stemmer(token):
        """Enhanced rule-based Amharic stemmer."""
        prefixes = ['በ', 'ከ', 'ለ', 'ወደ', 'እኩዴ', 'አ', 'ተ', 'ይ', 'እ', 'አል', 'ም', 'የ', 'ል', 'እን', 'ት', 'ነ', 'ንድ', 'አት', 'ይህ', 'እህ', 'ተን', 'አም', 'አን', 'በል', 'ከም', 'አላ', 'ዕ', 'ምን', 'ተአ']
        suffixes = ['ች', 'ው', 'ሮች', 'ዎች', 'ኔ', 'ከ', 'እ', 'ችው', 'ን', 'ም', 'ት', 'ሁ', 'ኩ', 'ውች', 'ያት', 'ማ', 'ቶች', 'ነት', 'ዬ', 'ኤ', 'ቸው', 'ሆች', 'ያ', 'ህ', 'ሃ', 'ንዎች', 'ቸ', 'ሳ', 'ብዎ', 'ል', 'ኞች', 'ሽ', 'ዋ', 'ሶ', 'በት', 'ችም', 'እት', 'ዋት', 'ኦች', 'ኖች', 'እያ', 'ኧት', 'እል', 'ልም', 'ችዋት', 'ቸዋል', 'ቸኝ', 'ችሁ', 'ቸሁ', 'ንህ', 'ችኋ', 'ለት', 'ንት', 'ንኝ', 'ኸ', 'ዋቸው', 'ያቸው', 'ምም', 'ሳት', 'ንኝህ', 'ትዎ', 'ንኝው', 'ቸኝ', 'ላት', 'ትን', 'ንዎ', 'ኸኝ', 'ዋቸ', 'ያትኝ', 'ምት', 'ሳው', 'ትዋ', 'ነትም', 'ችኝ', 'ልኝ', 'ትኝ']
        original = token
        for _ in range(2):
            for prefix in prefixes:
                if token.startswith(prefix):
                    token = token[len(prefix):]
                    break
            else:
                break
        for _ in range(2):
            for suffix in suffixes:
                if token.endswith(suffix):
                    token = token[:-len(suffix)]
                    break
            else:
                break
        if len(token) < 2 or not all(ord(c) in range(0x1200, 0x1380) or c == '_' for c in token):
            return original
        return token

    indexed_terms = [simple_amharic_stemmer(token) for token in tokens]

    return initial_tokens, indexed_terms