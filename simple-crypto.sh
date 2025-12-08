#!/bin/bash

# Simple XML Encoding/Decoding Script for macOS/Linux
# Uses base64 encoding with UTF-8 support - no external tools required

# Set UTF-8 locale to handle international characters properly
export LANG=en_US.UTF-8
export LC_ALL=en_US.UTF-8

# Function to encode XML file
encode_xml() {
    read -p "Enter the path of the XML file to encode: " xml_file
    read -p "Enter the output path for the encoded file: " output_file
    
    # Check if input file exists
    if [ ! -f "$xml_file" ]; then
        echo "Error: Input file does not exist!"
        return 1
    fi
    
    # Compress and encode the XML file using gzip and base64 with UTF-8 support
    echo "Compressing and encoding file (UTF-8)..."
    # Ensure UTF-8 encoding and compress, then base64 encode
    LC_ALL=en_US.UTF-8 gzip -c "$xml_file" | base64 > "$output_file"
    
    if [ $? -eq 0 ]; then
        echo "✓ XML file compressed and encoded successfully!"
        echo "Output: $output_file"
        # Show compression ratio
        original_size=$(stat -f%z "$xml_file" 2>/dev/null || stat -c%s "$xml_file" 2>/dev/null)
        compressed_size=$(stat -f%z "$output_file" 2>/dev/null || stat -c%s "$output_file" 2>/dev/null)
        if [ -n "$original_size" ] && [ -n "$compressed_size" ]; then
            echo "Original size: $original_size bytes"
            echo "Encoded size: $compressed_size bytes"
        fi
    else
        echo "✗ Encoding failed! Check file permissions and try again."
    fi
}

# Function to decode XML file
decode_xml() {
    read -p "Enter the path of the encoded file to decode: " encoded_file
    read -p "Enter the output path for the decoded file: " output_file
    
    # Check if input file exists
    if [ ! -f "$encoded_file" ]; then
        echo "Error: Input file does not exist!"
        return 1
    fi
    
    # Decode and decompress the XML file with UTF-8 support
    echo "Decoding and decompressing file (UTF-8)..."
    
    # Set UTF-8 locale for proper character handling
    export LC_ALL=en_US.UTF-8
    
    # Check if file has certificate headers and remove them
    if head -n 1 "$encoded_file" | grep -q "BEGIN CERTIFICATE"; then
        echo "Detected certificate format, removing headers..."
        sed '1d;$d' "$encoded_file" | base64 -D | gzip -d > "$output_file"
    else
        # Standard base64 decode and gzip decompress
        base64 -D < "$encoded_file" | gzip -d > "$output_file"
    fi
    
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

# Main Menu
while true; do
    echo ""
    echo "========================================"
    echo "     Simple XML Encoding Tool"
    echo "========================================"
    echo ""
    echo "Choose an option:"
    echo "1. Encode XML (Compress + Base64)"
    echo "2. Decode XML (Base64 + Decompress)"
    echo "3. Exit"
    echo ""
    read -p "Enter your choice (1-3): " choice

    case $choice in
        1)
            encode_xml
            ;;
        2)
            decode_xml
            ;;
        3)
            echo ""
            echo "Goodbye!"
            exit 0
            ;;
        *)
            echo "Invalid option. Please try again."
            ;;
    esac
done