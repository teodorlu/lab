(ns learn.thing-geom-1
  (:require
   [thi.ng.geom.core :as g]
   [thi.ng.geom.matrix :as mat]
   [thi.ng.geom.mesh.subdivision :as sd]
   [thi.ng.geom.gmesh :as gm]
   [thi.ng.geom.svg.shaders :as shader]
   [thi.ng.math.core :as m]
   [thi.ng.geom.circle :as c]
   [nextjournal.clerk :as clerk]))

(defmacro donil [& forms]
  `(do ~@forms nil))

(donil
  (def width    640)
  (def height   480))

(donil
 (def model    (g/rotate-y (mat/matrix44) m/SIXTH_PI))
 (def view     (apply mat/look-at (mat/look-at-vectors 0 1.75 0.75 0 0 0)))
 (def proj     (mat/perspective 60 (/ width height) 0.1 10))
 (def mvp      (->> model (m/* view) (m/* proj)))

 (def diffuse  (shader/normal-rgb (g/rotate-y (mat/matrix44) m/PI)))
 (def uniforms {:stroke "white" :stroke-width 0.25})
 )

(def shader-diffuse
  (shader/shader
   {:fill     diffuse
    :uniforms uniforms
    :flags    {:solid true}}))

(def shader-lambert
  (shader/shader
   {:fill     (shader/lambert
               {:view      view
                :light-dir [-1 0 1]
                :light-col [1 1 1]
                :diffuse   diffuse
                :ambient   0.1})
    :uniforms uniforms
    :flags    {:solid true}}))

(def shader-phong
  (shader/shader
   {:fill     (shader/phong
               {:model     model
                :view      view
                :light-pos [-1 2 1]
                :light-col [1 1 1]
                :diffuse   diffuse
                :ambient   [0.05 0.05 0.2]
                :specular  0.8
                :shininess 8.0})
    :uniforms uniforms
    :flags    {:solid true}}))

(defn ring
  [res radius depth wall]
  (-> (c/circle radius)
      (g/as-polygon res)
      (g/extrude-shell {:depth depth :wall wall :inset -0.1 :mesh (gm/gmesh)})
      (g/center)))

#_
(def mesh
  (->> [[1 0.25 0.15] [0.75 0.35 0.1] [0.5 0.5 0.05] [0.25 0.75 0.05]]
       (map (partial apply ring 40))
       (reduce g/into)
       (sd/catmull-clark)
       (sd/catmull-clark)))

^{:nextjournal.clerk/visibility {:code :hide}}
(clerk/html [:div {:style {:height "50vh"}}])
