import tkinter as tk
from tkinter import scrolledtext, messagebox
import logging

def create_gui(search_fn):
    """Create GUI with Amharic font, input validation, and enhanced feedback."""
    window = tk.Tk()
    window.title("Amharic Information Retrieval System")
    window.geometry("800x600")
    window.configure(bg="#f0f0f0")

    # Status bar
    status_var = tk.StringVar()
    status_var.set("Ready")
    status_bar = tk.Label(window, textvariable=status_var, bd=1, relief=tk.SUNKEN, anchor=tk.W, bg="#e0e0e0")
    status_bar.pack(side=tk.BOTTOM, fill=tk.X)

    tk.Label(window, text="Enter Amharic Query:", font=("Nyala", 14), bg="#f0f0f0").pack(pady=10)
    query_entry = tk.Entry(window, width=50, font=("Nyala", 12))
    query_entry.pack(pady=10)

    result_area = scrolledtext.ScrolledText(window, width=70, height=20, font=("Nyala", 12), bg="#ffffff")
    result_area.pack(pady=10)

    def search():
        query = query_entry.get().strip()
        if not query:
            messagebox.showwarning("Warning", "Please enter a query.")
            status_var.set("Warning: Please enter a query.")
            return
        if not all(ord(c) in range(0x1200, 0x1380) or c.isspace() for c in query):
            messagebox.showwarning("Warning", "Please enter a query in Amharic.")
            status_var.set("Warning: Use Amharic characters only.")
            return

        status_var.set("Searching...")
        window.update()
        try:
            results = search_fn(query)
            result_area.delete(1.0, tk.END)
            if not results:
                result_area.insert(tk.END, "No results found.\n")
                status_var.set("No results found.")
            else:
                for doc_id, score in results:
                    result_area.insert(tk.END, f"Document: {doc_id}, Score: {score:.4f}\n")
                status_var.set(f"Found {len(results)} results.")
            logging.info("Search completed successfully")
        except Exception as e:
            logging.error(f"Search error: {e}")
            messagebox.showerror("Error", "An error occurred during search.")
            status_var.set("Error occurred.")
        window.update()

    def clear():
        query_entry.delete(0, tk.END)
        result_area.delete(1.0, tk.END)
        status_var.set("Cleared")

    tk.Button(window, text="Search", command=search, font=("Nyala", 12), bg="#4CAF50", fg="white").pack(pady=5)
    tk.Button(window, text="Clear", command=clear, font=("Nyala", 12), bg="#ff9800", fg="white").pack(pady=5)
    window.mainloop()