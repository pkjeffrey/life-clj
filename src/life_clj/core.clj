(ns life-clj.core)

(def width (atom 10))
(def height (atom 10))

(def under-population 1)
(def over-population 4)
(def birth-population 3)

(defn wrap-cell
  [cell]
  [(mod (cell 0) @width) (mod (cell 1) @height)])

(defn neighbours
  "Returns a lazy sequence of cells that are neighbours to cell."
  [cell]
  (let [d (range -1 2)]
    (for [dx d
          dy d
          :when (not (and (zero? dx) (zero? dy)))]
      (wrap-cell [(+ (cell 0) dx) (+ (cell 1) dy)]))))

(defn live-neighbours
  "Returns a set of cells which are the live neighbours to cell."
  [live-cells cell]
  (->> cell
       (neighbours)
       (set)
       (clojure.set/intersection live-cells)))

(defn live-neighbour-count
  "Returns a count of the live neighbours to cell."
  [live-cells cell]
  (count (live-neighbours live-cells cell)))

(defn update-candidates
  "Returns a set of cells whcih are the candidates for update."
  [live-cells]
  (->> live-cells
       (map neighbours)
       (reduce concat)
       (concat live-cells)
       (set)))

(defn live-after-update
  "Returns the cell if it will be alive after update."
  [live-cells cell]
  (let [live-count (live-neighbour-count live-cells cell)]
    (if (live-cells cell)
      (when (and (> live-count under-population) (< live-count over-population))
        cell)
      (when (= live-count birth-population)
        cell))))

(defn next-live-cells
  [live-cells]
  (disj (->> live-cells
             (update-candidates)
             (map (partial live-after-update live-cells))
             (set))
        nil))
