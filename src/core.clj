(ns core
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as gen]
            [clojure.data.json :as json]
            [org.httpkit.client :as client]
            ;; strgen generates strings from regexes
            [miner.strgen :as sg]
            ;; exchange with real config
            [config-sample :as conf]))

(comment
  (gen/sample (sg/string-generator #"[A-Z]{2,4}"))

  ,,,)



;; define some regex patterns

(def ticket-pattern #"^[A-Z]*#?[0-9]+$")
(def transaction-pattern #"^[A-Z]+-[0-9]{2}[A-Z]{1}[0-9]{2}-[0-9]{6}-[0-9]{8}-[0-9a-zA-Z]{5}$")

;; define standalone specs

(s/def ::transaction-number (s/and string? #(re-matches transaction-pattern %)))
(s/def ::otrs-ticket-number (s/and string? #(re-matches ticket-pattern %)))

;; define the map spec

(s/def ::payload (s/keys :req [::otrs-ticket-number ::transaction-number]))

;; try out data generation
(gen/generate (s/gen int?))
;; => 361536
(gen/generate (s/gen string?))
;; => "GYZhfmkKn63pj475mFFax0c6AZkm"

(gen/generate (s/gen ::transaction-number)) ;; not possible within 100 tries because of "string AND regex"
(gen/generate (s/gen ::otrs-ticket-number)) ;; mostly not possible within 100 tries
;; => "T0"
(gen/generate (s/gen ::payload))

;; use strgen to generate string from regex
(gen/generate (sg/string-generator ticket-pattern))
;; => "SP003126090"
(gen/generate (sg/string-generator transaction-pattern))
;; => "UIODV-52U44-732397-18862920-9TyAA"

;; define standalone specs with custom generator

(s/def ::transactionNumber (s/spec (s/and string? #(re-matches transaction-pattern %))
                                   :gen #(sg/string-generator transaction-pattern)))

(s/def ::otrsTicketNumber (s/spec (s/and string? #(re-matches ticket-pattern %))
                                  :gen #(sg/string-generator ticket-pattern)))

(s/def ::payload-2 (s/keys :req [::otrsTicketNumber ::transactionNumber]))

;; now try to generate the data

(gen/generate (s/gen ::payload-2))
;; => #:core{:otrsTicketNumber "417292", :transactionNumber "NQN-73J76-873754-04494485-4BaG8"}

(gen/sample (s/gen ::payload-2))
;; => (#:core{:otrsTicketNumber "#0",
;;            :transactionNumber "N-77A40-459606-11494548-e8ssL"}
;;     #:core{:otrsTicketNumber "N1",
;;            :transactionNumber "C-73C39-482463-01167286-uTFOX"}
;;     #:core{:otrsTicketNumber "8",
;;            :transactionNumber "JR-02Q48-605183-13175434-vaKIU"}
;;     #:core{:otrsTicketNumber "M14",
;;            :transactionNumber "DO-85J85-674752-18296377-USNJm"}
;;     #:core{:otrsTicketNumber "VEY#47",
;;            :transactionNumber "WHH-35M98-230360-33295628-LIWmE"}
;;     #:core{:otrsTicketNumber "QLME4",
;;            :transactionNumber "B-02A93-639435-04672539-xyzGm"}
;;     #:core{:otrsTicketNumber "R7",
;;            :transactionNumber "EIFGZ-68C96-395868-86484690-7eBkz"}
;;     #:core{:otrsTicketNumber "#6",
;;            :transactionNumber "VNQE-83V97-476838-47123132-DtBrJ"}
;;     #:core{:otrsTicketNumber "ACJZLI#45796",
;;            :transactionNumber "YM-64V51-277119-07085333-06gem"}
;;     #:core{:otrsTicketNumber "C#54926070",
;;            :transactionNumber "HG-89Y49-957027-95788233-TFkq5"})
;; convert to json

(json/write-str (gen/generate (s/gen ::payload-2)))
;; => "{\"otrsTicketNumber\":\"ESVLFYSD5\",\"transactionNumber\":\"BFN-05W74-425566-21520362-yXvuI\"}"

;; make API call
(defn make-request [url headers payload-spec]
  (->
   @(client/post url
                 {:headers headers
                  :accept :json
                  :body (json/write-str (gen/generate (s/gen payload-spec)))})
   :status))

(make-request conf/route conf/headers ::payload-2)


