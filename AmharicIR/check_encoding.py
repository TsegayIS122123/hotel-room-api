import chardet
import os
import logging

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(levelname)s:%(name)s:%(message)s')

def check_encodings():
    """Check encoding of stopwords and corpus files."""
    files = [r"C:\Users\HP\Desktop\AmharicIR\stopwords.txt"] + \
            [os.path.join(r"C:\Users\HP\Desktop\AmharicIR\Input", f"AmharicBook{i}.txt") for i in range(1, 18)]
    
    for file in files:
        if os.path.exists(file):
            with open(file, 'rb') as f:
                result = chardet.detect(f.read())
                confidence = result['confidence']
                encoding = result['encoding'] if confidence > 0.7 else 'unknown'
                logging.info(f"Detected encoding for {file}: {encoding} (confidence: {confidence})")
                if encoding != 'utf-8' or confidence < 0.7:
                    logging.warning(f"Potential encoding issue with {file}. Expected UTF-8, got {encoding} (confidence: {confidence}).")
        else:
            logging.error(f"{file}: File not found")

if __name__ == "__main__":
    check_encodings()