(ns ^{:doc "This library is used to render LaTex Math equations, based
   on the jLateXMath library, and applying them incanter.charts as annotations
   and subtitles.
            "
       :author "David Edgar Liebke"}

  incanter.latex
  (:import [org.scilab.forge.jlatexmath TeXConstants TeXIcon TeXFormula])
  (:use [incanter.core :only [dim]]))



(defn latex 
" Returns the given LaTeX equation rendered as an java.awt.Image.

  Options:
    :color (default java.awt.Color/black) -- the text color
    :background (default java.awt.Clolor/white) -- the background color
    :border (default [5 5 5 5]) -- image border

  Examples:
    (use '(incanter core charts stats latex))

    (def latex-img (latex \"\\\\frac{(a+b)^2} {(a-b)^2}\"))
    (save latex-img \"/tmp/latex-example1.png\")
    (view \"file:///tmp/latex-example1.png\")

    (view (latex \"f(x)=\\\\frac {1} {\\\\sqrt {2\\\\pi \\\\sigma ^2}} e^{\\\\frac {-(x - \\\\mu)^2}{2 \\\\sigma ^2}}\"))

    (view (latex \"\\\\begin{pmatrix}
                   a & b & c \\\\\\\\
                   d & e & f \\\\\\\\
                   g & h & i
                   \\\\end{pmatrix}\"))


"
([latex-txt & {:keys [color background border] :or {color java.awt.Color/black
                                                    background java.awt.Color/white
                                                    border [5 5 5 5]}}]
     (let [formula (org.scilab.forge.jlatexmath.TeXFormula. latex-txt)
           icon (doto (.createTeXIcon formula TeXConstants/STYLE_DISPLAY 20)
                  (.setInsets (apply #(java.awt.Insets. %1 %2 %3 %4) border)))
           image (java.awt.image.BufferedImage. (.getIconWidth icon) 
                                                (.getIconHeight icon) 
                                                java.awt.image.BufferedImage/TYPE_INT_ARGB)
           g2 (doto (.createGraphics image)
                (.setColor background)
                (.fillRect 0 0 (.getIconWidth icon) (.getIconHeight icon)))
           label (doto (javax.swing.JLabel.)
                   (.setForeground color))]
       (do
         (.paintIcon icon label g2 0 0)
         image))))






(defn add-latex-subtitle
" Adds the given LaTeX equation as a subtitle to the chart.


  Options:
    :color (default java.awt.Color/darkGray) -- the text color


  Examples:
    (use '(incanter core charts stats latex))

    (doto (function-plot pdf-normal -3 3)
      (add-latex-subtitle \"f(x)=\\\\frac{1}{\\\\sqrt{2\\\\pi \\\\sigma^2}} e^{\\\\frac{-(x - \\\\mu)^2}{2 \\\\sigma^2}}\")
      view)

"
([chart latex-str & {:keys [color] :or {color java.awt.Color/darkGray}}]
   (.addSubtitle chart (org.jfree.chart.title.ImageTitle. (latex latex-str :color color)))
   chart))




(defn add-latex
" Adds an LaTeX equation annotation to the chart at the given x,y coordinates.

  Arguments:
    chart -- the chart to add the polygon to.
    x, y -- the coordinates to place the image
    latex-str -- a string of latex code


  Options:
    :color (default java.awt.Color/darkGray) -- the text color


  Examples:
    (use '(incanter core charts stats latex))   

      (doto (function-plot pdf-normal -3 3)
        (add-latex 0 0.1 \"f(x)=\\\\frac{1}{\\\\sqrt{2\\\\pi \\\\sigma^2}} e^{\\\\frac{-(x - \\\\mu)^2}{2 \\\\sigma^2}}\")
        view)

"
([chart x y latex-str & {:keys [color] :or {color java.awt.Color/darkGray}}]
   (let [img (latex latex-str :color color)
         anno (org.jfree.chart.annotations.XYImageAnnotation. x y img)]
     (.addAnnotation (.getPlot chart) anno)
     chart)))

(defn to-latex
  "Convert an Incanter Matrix into a string of LaTeX commands to render it.

Options:
  :mxtype (default pmatrix) -- the type of matrix to output, see LaTeX documentation for other options.
Example:
    (use '(incanter core latex))
    (view (latex (to-latex (matrix [[1 0][0 1]]))))
"
  [mx &
   {:keys [mxtype]
    :or {mxtype "pmatrix"}}]
  (let [dimensions (zipmap [:height :width] (dim mx))
        write-row (fn [coll] (apply str (interpose " & " coll)))]
   (str
    "\\begin{" mxtype "}"
    (apply
     str
     (interpose
      "\\\\"
      (map
       write-row
       (map
        (partial nth mx)
        (range (:height dimensions))))))
    "\\end{" mxtype "}")))
