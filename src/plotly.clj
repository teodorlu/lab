(ns plotly
  (:require
   [clojure.string :as str]
   [nextjournal.clerk :as clerk]))

;; Reading https://plotly.com/javascript/3d-mesh/, I think plotly can be used to draw in 3D.

(clerk/plotly {:data [{:z [[1 2 3] [3 2 1]] :type "surface"}]
               :layout {:margin {:l 20 :r 0 :b 20 :t 20}}
               :config {:displayModeBar false
                        :displayLogo false}})

;; Converting https://plotly.com/javascript/3d-mesh/ to Clojure.

(let [a (repeatedly 50 rand)
      b (repeatedly 50 rand)
      c (repeatedly 50 rand)]
  (clerk/plotly {:data [{:alphahull 5
                         :opacity 0.8
                         :color "rgb(200,100,300)"
                         :type "mesh3d"
                         :x a
                         :y b
                         :z c}]}))

;; Based on the following javascript:

^{:nextjournal.clerk/visibility {:code :hide}}
(let [js (str/trim "
a=[]; b=[]; c=[];
for (i=0; i<50; i++){
  var a_ = Math.random();
  a.push(a_);
  var b_ = Math.random();
  b.push(b_);
  var c_ = Math.random();
  c.push(c_);
}

// Plotting the mesh
var data=[
  {
    alphahull:5,
    opacity:0.8,
    color:'rgb(200,100,300)',
    type: 'mesh3d',
    x: a,
    y: b,
    z: c,
  }
];

Plotly.newPlot('myDiv', data);
")]
  (clerk/md (str "```javascript"
                 js
                 "```")))
