#!/usr/bin/env python3
import subprocess
import itertools
import os
from collections import defaultdict

# Configuration
WEIGHT_RANGE = [round(x * 0.1, 1) for x in range(1, 21)]  # 0.1 to 2.0
RUNS_PER_COMBO = 100
COMMAND = ["java", "-cp", "target/classes/", 
           "com.javanabi.HanabiServer", "BestValueHint", "BestValueHint"]

weight_names = ["WEIGHT_MATCHED", "WEIGHT_COMPLETE", "WEIGHT_PLAYABLE", 
                "WEIGHT_DISCARDABLE", "WEIGHT_FINAL_CARD"]

best_combo = None
best_avg_score = -1
results = []

total_combos = len(WEIGHT_RANGE) ** 5
current_combo_num = 0

print(f"Testing {total_combos} weight combinations, {RUNS_PER_COMBO} runs each...")
print(f"Total command executions: {total_combos * RUNS_PER_COMBO}\n")

# Iterate through all weight combinations
for weights in itertools.product(WEIGHT_RANGE, repeat=5):
    current_combo_num += 1
    
    # Set environment variables
    env = os.environ.copy()
    for name, weight in zip(weight_names, weights):
        env[name] = str(weight)
    
    scores = []
    
    # Run command 100 times
    for run in range(RUNS_PER_COMBO):
        try:
            result = subprocess.run(COMMAND, env=env, capture_output=True, 
                                    timeout=10)
            score = result.returncode
            scores.append(score)
        except subprocess.TimeoutExpired:
            print(f"Timeout for combo {weights}")
            scores.append(0)
        except Exception as e:
            print(f"Error running command: {e}")
            scores.append(0)
    
    avg_score = sum(scores) / len(scores)
    results.append((weights, avg_score, scores))
    
    # Update best
    if avg_score > best_avg_score:
        best_avg_score = avg_score
        best_combo = weights
    
    # Progress indicator
    if current_combo_num % 1000 == 0:
        print(f"Progress: {current_combo_num}/{total_combos} | "
              f"Best so far: {best_combo} = {best_avg_score:.2f}")

# Sort results by average score
results.sort(key=lambda x: x[1], reverse=True)

# Print top 10 results
print("\n" + "="*70)
print("TOP 10 RESULTS:")
print("="*70)
for i, (weights, avg_score, _) in enumerate(results[:10], 1):
    print(f"{i}. Avg Score: {avg_score:.2f}")
    for name, weight in zip(weight_names, weights):
        print(f"   {name}: {weight}")
    print()

# Print best result
print("="*70)
print("BEST CONFIGURATION:")
print("="*70)
for name, weight in zip(weight_names, best_combo):
    print(f"export {name}={weight}")
