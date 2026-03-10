#!/bin/bash

# Enhanced XML Encoding/Decoding Script with Compression for macOS/Linux
# Uses base64 encoding with UTF-8 support and aggressive compression
# Usage: 
#   Interactive: ./simple-crypto-2.sh
#   Command-line: ./simple-crypto-2.sh encode <input> <output>
#   Command-line: ./simple-crypto-2.sh encode-ultra <input> <output>
#   Command-line: ./simple-crypto-2.sh decode <input> <output>

# Set UTF-8 locale to handle international characters properly
export LANG=en_US.UTF-8
export LC_ALL=en_US.UTF-8

# Function to encode XML file with maximum compression
encode_xml() {
    local xml_file="$1"
    local output_file="$2"
    
    if [ -z "$xml_file" ]; then
        read -p "Enter the path of the XML file to encode: " xml_file
        read -p "Enter the output path for the encoded file: " output_file
    fi
    
    # Check if input file exists
    if [ ! -f "$xml_file" ]; then
        echo "Error: Input file does not exist!"
        return 1
    fi
    
    # Compress and encode the XML file using gzip with maximum compression and base64 with UTF-8 support
    echo "Compressing and encoding file with maximum compression (UTF-8)..."
    # Ensure UTF-8 encoding and compress with best compression (-9), then base64 encode
    LC_ALL=en_US.UTF-8 gzip -9 -c "$xml_file" | base64 > "$output_file"
    
    if [ $? -eq 0 ]; then
        echo "✓ XML file compressed and encoded successfully!"
        echo "Output: $output_file"
        # Show compression ratio
        original_size=$(stat -f%z "$xml_file" 2>/dev/null || stat -c%s "$xml_file" 2>/dev/null)
        compressed_size=$(stat -f%z "$output_file" 2>/dev/null || stat -c%s "$output_file" 2>/dev/null)
        if [ -n "$original_size" ] && [ -n "$compressed_size" ]; then
            echo "Original size: $original_size bytes"
            echo "Encoded size: $compressed_size bytes"
            ratio=$(awk "BEGIN {printf \"%.2f\", ($original_size / $compressed_size)}")
            echo "Compression ratio: ${ratio}:1"
        fi
    else
        echo "✗ Encoding failed! Check file permissions and try again."
    fi
}

# Function to encode XML file with ultra compression (using bzip2 for better compression)
encode_xml_ultra() {
    local xml_file="$1"
    local output_file="$2"
    
    if [ -z "$xml_file" ]; then
        read -p "Enter the path of the XML file to encode: " xml_file
        read -p "Enter the output path for the encoded file: " output_file
    fi
    
    # Check if input file exists
    if [ ! -f "$xml_file" ]; then
        echo "Error: Input file does not exist!"
        return 1
    fi
    
    # Check if bzip2 is available
    if ! command -v bzip2 &> /dev/null; then
        echo "Error: bzip2 is not installed. Using gzip instead..."
        encode_xml
        return
    fi
    
    # Compress and encode the XML file using bzip2 with maximum compression and base64
    echo "Compressing and encoding file with ULTRA compression (bzip2)..."
    # Use bzip2 for better compression ratio, then base64 encode
    LC_ALL=en_US.UTF-8 bzip2 -9 -c "$xml_file" | base64 > "$output_file"
    
    if [ $? -eq 0 ]; then
        echo "✓ XML file compressed (ULTRA) and encoded successfully!"
        echo "Output: $output_file"
        # Show compression ratio
        original_size=$(stat -f%z "$xml_file" 2>/dev/null || stat -c%s "$xml_file" 2>/dev/null)
        compressed_size=$(stat -f%z "$output_file" 2>/dev/null || stat -c%s "$output_file" 2>/dev/null)
        if [ -n "$original_size" ] && [ -n "$compressed_size" ]; then
            echo "Original size: $original_size bytes"
            echo "Encoded size: $compressed_size bytes"
            ratio=$(awk "BEGIN {printf \"%.2f\", ($original_size / $compressed_size)}")
            echo "Compression ratio: ${ratio}:1"
        fi
    else
        echo "✗ Encoding failed! Check file permissions and try again."
    fi
}

# Function to decode XML file (auto-detects gzip or bzip2)
decode_xml() {
    local encoded_file="$1"
    local output_file="$2"
    
    if [ -z "$encoded_file" ]; then
        read -p "Enter the path of the encoded file to decode: " encoded_file
        read -p "Enter the output path for the decoded file: " output_file
    fi
    
    # Check if input file exists
    if [ ! -f "$encoded_file" ]; then
        echo "Error: Input file does not exist!"
        return 1
    fi
    
    # Decode and decompress the XML file with UTF-8 support
    echo "Decoding and decompressing file (UTF-8)..."
    
    # Set UTF-8 locale for proper character handling
    export LC_ALL=en_US.UTF-8
    
    # Decode base64 to a temporary file
    temp_file=$(mktemp)
    
    # Check if file has certificate headers and remove them
    if head -n 1 "$encoded_file" | grep -q "BEGIN CERTIFICATE"; then
        echo "Detected certificate format, removing headers..."
        sed '1d;$d' "$encoded_file" | base64 -D > "$temp_file"
    else
        # Standard base64 decode
        base64 -D < "$encoded_file" > "$temp_file"
    fi
    
    # Auto-detect compression format by checking magic bytes
    if file "$temp_file" | grep -q "gzip"; then
        echo "Detected gzip compression..."
        gzip -d -c "$temp_file" > "$output_file"
    elif file "$temp_file" | grep -q "bzip2"; then
        echo "Detected bzip2 compression..."
        bzip2 -d -c "$temp_file" > "$output_file"
    else
        echo "Warning: Unknown compression format, attempting gzip..."
        gzip -d -c "$temp_file" > "$output_file"
    fi
    
    # Clean up temp file
    rm -f "$temp_file"
    
    if [ $? -eq 0 ]; then
        echo "✓ XML file decoded and decompressed successfully!"
        echo "Output: $output_file"
        # Show decompressed file size
        decompressed_size=$(stat -f%z "$output_file" 2>/dev/null || stat -c%s "$output_file" 2>/dev/null)
        if [ -n "$decompressed_size" ]; then
            echo "Decompressed size: $decompressed_size bytes"
        fi
    else
        echo "✗ Decoding failed! Check file format and integrity."
    fi
}

# Check for command-line arguments
if [ $# -ge 2 ]; then
    command="$1"
    input_file="$2"
    output_file="${3:-${input_file}.encoded}"
    
    case $command in
        encode|e)
            encode_xml "$input_file" "$output_file"
            exit $?
            ;;
        encode-ultra|eu)
            encode_xml_ultra "$input_file" "$output_file"
            exit $?
            ;;
        decode|d)
            decode_xml "$input_file" "$output_file"
            exit $?
            ;;
        *)
            echo "Invalid command. Use: encode, encode-ultra, or decode"
            exit 1
            ;;
    esac
fi

# Main Menu
while true; do
    echo ""
    echo "========================================"
    echo "  Enhanced XML Encoding Tool v2"
    echo "  with Advanced Compression"
    echo "========================================"
    echo ""
    echo "Choose an option:"
    echo "1. Encode XML (Compress + Base64) - Standard"
    echo "2. Encode XML (ULTRA Compress + Base64) - Best Ratio"
    echo "3. Decode XML (Base64 + Decompress) - Auto-detect"
    echo "4. Exit"
    echo ""
    read -p "Enter your choice (1-4): " choice
    
    # Exit on EOF (Ctrl+D) or empty input
    if [ -z "$choice" ]; then
        echo ""
        echo "Goodbye!"
        exit 0
    fi

    case $choice in
        1)
            encode_xml
            ;;
        2)
            encode_xml_ultra
            ;;
        3)
            decode_xml
            ;;
        4)
            echo ""
            echo "Goodbye!"
            exit 0
            ;;
        *)
            echo "Invalid option. Please try again."
            ;;
    esac
done
