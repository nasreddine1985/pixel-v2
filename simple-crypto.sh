#!/bin/bash

# Simple XML Encoding/Decoding Script for macOS/Linux
# Uses base64 encoding - no external tools required

# Function to encode XML file
encode_xml() {
    read -p "Enter the path of the XML file to encode: " xml_file
    read -p "Enter the output path for the encoded file: " output_file
    
    # Check if input file exists
    if [ ! -f "$xml_file" ]; then
        echo "Error: Input file does not exist!"
        return 1
    fi
    
    # Encode the XML file using base64
    echo "Encoding file..."
    base64 -i "$xml_file" -o "$output_file"
    
    if [ $? -eq 0 ]; then
        echo "✓ XML file encoded successfully!"
        echo "Output: $output_file"
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
    
    # Decode the XML file
    echo "Decoding file..."
    
    # Check if file has certificate headers and remove them
    if head -n 1 "$encoded_file" | grep -q "BEGIN CERTIFICATE"; then
        echo "Detected certificate format, removing headers..."
        sed '1d;$d' "$encoded_file" | base64 -D > "$output_file"
    else
        # Standard base64 decode
        base64 -D -i "$encoded_file" -o "$output_file"
    fi
    
    if [ $? -eq 0 ]; then
        echo "✓ XML file decoded successfully!"
        echo "Output: $output_file"
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
    echo "1. Encode XML (Base64)"
    echo "2. Decode XML (Base64)"
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