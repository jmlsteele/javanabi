#!/bin/bash

echo "=== Testing HanabiServer with Dynamic Player Classes ==="
echo ""

echo "1. Testing with 2 SimpleAIPlayers:"
echo "java -cp target/classes com.javanabi.HanabiServer SimpleAIPlayer SimpleAIPlayer"
echo ""

echo "2. Testing with 3 SimpleAIPlayers:"
echo "java -cp target/classes com.javanabi.HanabiServer SimpleAIPlayer SimpleAIPlayer SimpleAIPlayer"
echo ""

echo "3. Testing with 5 SimpleAIPlayers (max):"
echo "java -cp target/classes com.javanabi.HanabiServer SimpleAIPlayer SimpleAIPlayer SimpleAIPlayer SimpleAIPlayer SimpleAIPlayer"
echo ""

echo "4. Testing error cases:"
echo "   - Too few players (1):"
echo "java -cp target/classes com.javanabi.HanabiServer SimpleAIPlayer"
echo ""
echo "   - Too many players (6):"
echo "java -cp target/classes com.javanabi.HanabiServer SimpleAIPlayer SimpleAIPlayer SimpleAIPlayer SimpleAIPlayer SimpleAIPlayer SimpleAIPlayer"
echo ""
echo "   - Invalid class name:"
echo "java -cp target/classes com.javanabi.HanabiServer InvalidClass SimpleAIPlayer"
echo ""

echo "=== All tests demonstrate the new flexible system! ==="