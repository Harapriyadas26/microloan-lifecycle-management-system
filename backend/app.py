from flask import Flask, request, jsonify, send_file
import sqlite3
import os
import uuid
from datetime import datetime
from werkzeug.utils import secure_filename

app = Flask(__name__)

UPLOAD_FOLDER = "kyc_documents"
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

app.config["MAX_CONTENT_LENGTH"] = 5 * 1024 * 1024

DB = "microloan.db"



# Database connection
def get_db():
    conn = sqlite3.connect(DB)
    conn.row_factory = sqlite3.Row
    return conn


# Create tables
def init_db():
    with get_db() as conn:
        conn.execute("""
            CREATE TABLE IF NOT EXISTS documents (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                borrower_id INTEGER,
                document_type TEXT,
                filename TEXT,
                status TEXT DEFAULT 'Pending',
                uploaded_at TEXT
            )
        """)

        conn.execute("""
            CREATE TABLE IF NOT EXISTS verification_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                document_id INTEGER,
                action TEXT,
                reason TEXT,
                verified_at TEXT
            )
        """)


# 1. Upload KYC document
@app.route("/upload", methods=["POST"])
def upload_document():

    borrower_id = request.form.get("borrower_id")
    document_type = request.form.get("document_type")
    file = request.files.get("file")

    if not borrower_id or not borrower_id.isdigit():
        return jsonify({"error": "Valid borrower ID required"}), 400

    if document_type not in ["Aadhaar", "PAN"]:
        return jsonify({"error": "Invalid document type"}), 400

    if not file or not file.filename:
        return jsonify({"error": "File required"}), 400

    extension = os.path.splitext(
        secure_filename(file.filename)
    )[1].lower()

    if extension not in [".pdf", ".jpg", ".jpeg", ".png"]:
        return jsonify({"error": "Invalid file format"}), 400

    filename = str(uuid.uuid4()) + extension
    file.save(os.path.join(UPLOAD_FOLDER, filename))

    with get_db() as conn:
        cursor = conn.execute("""
            INSERT INTO documents
            (borrower_id, document_type, filename, uploaded_at)
            VALUES (?, ?, ?, ?)
        """, (
            borrower_id,
            document_type,
            filename,
            datetime.now().isoformat()
        ))

        document_id = cursor.lastrowid

    return jsonify({
        "message": "Document uploaded successfully",
        "document_id": document_id,
        "status": "Pending"
    }), 201


# 2. View submitted documents
@app.route("/documents", methods=["GET"])
def view_documents():

    with get_db() as conn:
        documents = conn.execute(
            "SELECT * FROM documents"
        ).fetchall()

    return jsonify([dict(doc) for doc in documents])


# 3. Admin verifies or rejects document
@app.route("/verify/<int:document_id>", methods=["POST"])
def verify_document(document_id):

    data = request.get_json(silent=True) or {}
    status = data.get("status")
    reason = data.get("reason", "")

    if status not in ["Approved", "Rejected"]:
        return jsonify({"error": "Invalid status"}), 400

    if status == "Rejected" and not reason.strip():
        return jsonify({"error": "Rejection reason required"}), 400

    with get_db() as conn:

        document = conn.execute(
            "SELECT * FROM documents WHERE id = ?",
            (document_id,)
        ).fetchone()

        if not document:
            return jsonify({"error": "Document not found"}), 404

        conn.execute("""
            UPDATE documents
            SET status = ?
            WHERE id = ?
        """, (status, document_id))

        conn.execute("""
            INSERT INTO verification_history
            (document_id, action, reason, verified_at)
            VALUES (?, ?, ?, ?)
        """, (
            document_id,
            status,
            reason,
            datetime.now().isoformat()
        ))

    return jsonify({
        "message": "KYC verification completed",
        "status": status
    })


# 4. Verification history
@app.route("/history/<int:document_id>", methods=["GET"])
def verification_history(document_id):

    with get_db() as conn:
        history = conn.execute("""
            SELECT * FROM verification_history
            WHERE document_id = ?
            ORDER BY id DESC
        """, (document_id,)).fetchall()

    return jsonify([dict(row) for row in history])


# 5. Protected document access (admin only)
@app.route("/document/<int:document_id>", methods=["GET"])
def access_document(document_id):

    # Demo only: replace with real session-based
    # admin authentication before deployment.
    if request.headers.get("X-Demo-Role") != "admin":
        return jsonify({"error": "Admin access required"}), 403

    with get_db() as conn:
        document = conn.execute(
            "SELECT filename FROM documents WHERE id = ?",
            (document_id,)
        ).fetchone()

    if not document:
        return jsonify({"error": "Document not found"}), 404

    return send_file(
        os.path.join(UPLOAD_FOLDER, document["filename"])
    )


if __name__ == "__main__":
    init_db()
    app.run(debug=True)
