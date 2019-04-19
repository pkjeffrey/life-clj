(ns life-clj.ui
  (:require [life-clj.core :as life])
  (:import [java.awt BorderLayout Color Dimension]
           [javax.swing JFrame JPanel]))

(def state (atom nil))

(def grid-size 10)

(defn game-thread
  [dur board]
  (proxy [Thread]
    []
    (run []
      (loop []
        (.repaint board)
        (Thread/sleep dur)
        (reset! state (life/next-live-cells @state))
        (recur)))))

(defn game-board
  []
  (proxy [JPanel]
    [] ; superclass contructor args
    (getPreferredSize []
      (Dimension. 800 600))
    (paintComponent [g]
      (proxy-super paintComponent g)
      (let [width (.getWidth this)
            height (.getHeight this)
            grid-width (quot width grid-size)
            grid-height (quot height grid-size)
            x-offset (quot (- width (* grid-width grid-size)) 2)
            y-offset (quot (- height (* grid-height grid-size)) 2)]
        (.setColor g (Color. 128 128 128))
        (dotimes [x (+ grid-width 1)]
          (.drawLine g (+ (* x grid-size) x-offset) y-offset (+ (* x grid-size) x-offset) (+ (* grid-height grid-size) y-offset)))
        (dotimes [y (+ grid-height 1)]
          (.drawLine g x-offset (+ (* y grid-size) y-offset) (+ (* grid-width grid-size) x-offset) (+ (* y grid-size) y-offset)))
        (.setColor g (Color. 255 153 0))
        (loop [live @state]
          (when-not (empty? live)
            (let [cell (first live)
                  x (cell 0)
                  y (cell 1)]
              (.fillRect g (+ (* x grid-size) x-offset 1) (+ (* y grid-size) y-offset 1) (- grid-size 1) (- grid-size 1)))
            (recur (rest live))))))))

(defn main
  [dur live]
  (let [frame (JFrame.)
        content-pane (.getContentPane frame)
        game-board (game-board)
        game-thread (game-thread dur game-board)]
    (.setBackground game-board (Color. 33 33 33))
    (.add content-pane game-board BorderLayout/CENTER)
    (doto frame
      (.setTitle "Conway's Game of Life")
      ;(.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE) commented out to run in repl
      (.setResizable false)
      (.pack)
      (.setVisible true))
    (reset! state live)
    (reset! life/width (quot (.getWidth game-board) grid-size))
    (reset! life/height (quot (.getHeight game-board) grid-size))
    (.start game-thread)))

;; glider => (main 100 #{[2 1] [3 2] [1 3] [2 3] [3 3]})
;; acorn  => (main 100 #{[37 28] [39 29] [36 30] [37 30] [40 30] [41 30] [42 30]})