package main

import (
	"fmt"
	"os"
	"os/exec"
	"runtime"
	"sort"
	"sync"
	"sync/atomic"
)

type Result struct {
	combo    [5]float64
	avgScore float64
}

var iterations int = 10
var increment float64 = 0.25
var initialWeights [5]float64 = [5]float64{0.5,1.5,1.5,1,1}
func main() {
	numWorkers := runtime.NumCPU()
	fmt.Printf("Using %d workers\n", numWorkers)

	// Generate all combinations
	combinations := generateCombinations(initialWeights,increment,2)
	fmt.Printf("Testing %d weight combinations, %d runs each...\n",
		len(combinations),iterations)
	fmt.Printf("Total command executions: %d\n\n", len(combinations)*iterations)

	// Channels
	comboChan := make(chan [5]float64, numWorkers*2)
	resultChan := make(chan Result, numWorkers*2)

	// Worker pool
	var wg sync.WaitGroup
	for i := 0; i < numWorkers; i++ {
		wg.Add(1)
		go worker(&wg, comboChan, resultChan)
	}

	// Send combinations
	go func() {
		for _, combo := range combinations {
			comboChan <- combo
		}
		close(comboChan)
	}()

	// Collect results with progress
	var results []Result
	var processed int64
	total := int64(len(combinations))

	go func() {
		for result := range resultChan {
			results = append(results, result)
			current := atomic.AddInt64(&processed, 1)
			if current%1000 == 0 {
				fmt.Printf("Progress: %d/%d\n", current, total)
			}
		}
	}()

	wg.Wait()
	close(resultChan)

	// Wait for collection to finish
	for len(results) < len(combinations) {
	}

	// Sort by average score (descending)
	sort.Slice(results, func(i, j int) bool {
		return results[i].avgScore > results[j].avgScore
	})

	// Print top 10 results
	weightNames := []string{"WEIGHT_MATCHED", "WEIGHT_COMPLETE",
		"WEIGHT_PLAYABLE", "WEIGHT_DISCARDABLE", "WEIGHT_FINAL_CARD"}

	fmt.Println("\n" +
		"====================================================================")
	fmt.Println("TOP 10 RESULTS:")
	fmt.Println(
		"====================================================================")

	for i := 0; i < 10 && i < len(results); i++ {
		fmt.Printf("%d. Avg Score: %.2f\n", i+1, results[i].avgScore)
		for j, name := range weightNames {
			fmt.Printf("   %s: %.1f\n", name, results[i].combo[j])
		}
		fmt.Println()
	}

	// Print best configuration
	fmt.Println(
		"====================================================================")
	fmt.Println("BEST CONFIGURATION:")
	fmt.Println(
		"====================================================================")
	best := results[0]
	for i, name := range weightNames {
		fmt.Printf("export %s=%.1f\n", name, best.combo[i])
	}
}

func generateCombinations(initial [5]float64, increment float64, count int) [][5]float64 {
	// Generate possible values for each dimension
	var weightSets [5][]float64

	for dim := 0; dim < 5; dim++ {
		var values []float64
		// Generate values from initial - count*increment to initial + count*increment
		for i := -count; i <= count; i++ {
			values = append(values, initial[dim]+float64(i)*increment)
		}
		weightSets[dim] = values
	}

	// Generate all combinations (Cartesian product)
	var combos [][5]float64
	for _, w0 := range weightSets[0] {
		for _, w1 := range weightSets[1] {
			for _, w2 := range weightSets[2] {
				for _, w3 := range weightSets[3] {
					for _, w4 := range weightSets[4] {
						combos = append(combos,
							[5]float64{w0, w1, w2, w3, w4})
					}
				}
			}
		}
	}
	return combos
}

func worker(wg *sync.WaitGroup, comboChan chan [5]float64,
	resultChan chan Result) {
	defer wg.Done()

	weightNames := []string{"WEIGHT_MATCHED", "WEIGHT_COMPLETE",
		"WEIGHT_PLAYABLE", "WEIGHT_DISCARDABLE", "WEIGHT_FINAL_CARD"}

	for combo := range comboChan {
		totalScore := 0
		for run := 0; run < iterations; run++ {
			cmd := exec.Command("java", "-cp", "target/classes/",
				"com.javanabi.HanabiServer", "BestValueHint",
				"BestValueHint")

			// Set environment variables
			env := os.Environ()
			for i, name := range weightNames {
				env = append(env,
					fmt.Sprintf("%s=%.1f", name, combo[i]))
			}
			cmd.Env = env

			cmd.Run()
			totalScore += cmd.ProcessState.ExitCode()
		}

		avgScore := float64(totalScore) / float64(iterations)
		resultChan <- Result{
			combo:    combo,
			avgScore: avgScore,
		}
	}
}
