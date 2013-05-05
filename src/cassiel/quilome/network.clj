(ns cassiel.quilome.network
  "OSC networking utilities - cargo-culted from eu.cassiel/cubewar."
  (:import [java.net InetAddress]
           [net.loadbang.osc.comms UDPTransmitter UDPReceiver]
           [net.loadbang.osc.data Bundle Message]
           [net.loadbang.osc.exn CommsException]))

(defn- dispatch-message
  "Unpack a message and call `f` with host/port, OSC address, and list of args."
  [f
   origin                               ; {:host, :port}
   ^Message m]
  (let [args (for
                 [i (range (.getNumArguments m))]
               (.getValue (.getArgument m i)))]
    (f origin (.getAddress m)  #_ (keyword address) args)))

(defn- start-input [rx]
  (.start (Thread. (reify Runnable
                       (run [this]
                         (try
                           (dorun (repeatedly #(.take rx)))
                           (catch CommsException _ nil)))))))

(defn- consume [f socket m]
  (let [host (.getHostName socket)
        port (.getPort socket)]
    (dispatch-message f {:host host :port port} m)))

(defn- make-receiver
  ([f]
     (proxy
         [UDPReceiver]
         []
       (consumeMessage [socket _date00 m] (consume f socket m))))
  ([port f]
     (proxy
         [UDPReceiver]
         [port]
       (consumeMessage [socket _date00 m] (consume f socket m)))))

(defn start-receiver
  "Create a receiver socket given a consuming function. The receiver accepts `(.close)`.
   unlike the Cubewar version, this one determines its own incoming port."
  ([f]
     (let [rx (make-receiver f)]
       (.open rx)
       (start-input rx)
       rx))
  ([port f]
     (let [rx (make-receiver port f)]
       (.open rx)
       (start-input rx)
       rx)))

(defn start-transmitter
  "Create a transmitter to host and port. The transmitter accepts `(.close)`."
  [host port]
  (UDPTransmitter. (InetAddress/getByName host) port))
