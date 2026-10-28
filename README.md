# Liman Feribot Yönetim Sistemi

Java ve JavaFX ile geliştirilmiş, bir limandaki feribot yükleme sürecini dakika dakika simüle eden masaüstü uygulaması. Veri Yapıları dersi projesi olarak hazırlanmıştır; kuyruk, yığın ve hash tablosu hazır Java koleksiyonları kullanılmadan sıfırdan yazılmıştır.

## Özellikler

- 08:00 – 20:00 arası liman simülasyonu (adım adım, duraklatmalı veya tek seferde tamamı)
- Gişeden gelen araçların sırayla iki yükleme yoluna dağıtılması
- Feribotun iki katı: **giriş katı** ağır vasıtalar, **üst kat** otomobiller için (her kat 5 araç)
- Kalkış kuralları: feribot tamamen dolarsa erken kalkar; kalkış saati geldiğinde bir kat tamamen veya iki kat en az %50 doluysa kalkar
- Canlı log paneli, rıhtım ve kat doluluk göstergeleri
- Arayüzden yeni araç ekleme (araç `Arac.txt` dosyasına da yazılır)
- Plaka tekrarını engelleyen hash tablosu ve içeriğini gösteren ayrı pencere

## Kullanılan Veri Yapıları

| Yapı | Sınıf | Kullanım |
|------|-------|----------|
| Kuyruk (Queue) | `datastructures/Kuyruk.java` | Yükleme yolları (ilk gelen ilk yüklenir) |
| Yığın (Stack) | `datastructures/Yigin.java` | Feribot katları (son giren ilk çıkar) |
| Hash Tablosu | `datastructures/HashTablosu.java` | Plaka kontrolü, ayrı zincirleme (separate chaining) ile çakışma çözümü |
| Bağlı Liste Düğümü | `datastructures/Node.java` | Kuyruk ve yığının temel düğümü |

## Gereksinimler

- Java 21
- Maven

## Çalıştırma

Proje kök dizininde:

```bash
mvn javafx:run
```

`Arac.txt` ve `Feribot.txt` dosyaları proje kök dizininden okunur, bu yüzden uygulamayı kök dizinden çalıştırın.

## Veri Dosyaları

**Feribot.txt** — `seferNo-feribotNo-rıhtımGirişSaati-planlananKalkışSaati-kalkışaHazırMı`

```
S101-01-09.00-10.00-false
```

**Arac.txt** — `seferNo-araçNo-plaka-gişeGirişSaati-araçTipi`  (araç tipi: `1` = ağır vasıta, `2` = otomobil; sefer ve araç numarası simülasyonda atanır, dosyada `*` yazılır)

```
*-*-63HRU01-13.05-1
```

## Proje Yapısı

```
src/main/java/
├── Main.java                 # JavaFX giriş noktası
├── datastructures/           # Kuyruk, Yigin, HashTablosu, Node
├── model/                    # Arac, Feribot
├── logic/                    # SimulationController (simülasyon mantığı)
└── gui/                      # Arayüz controller'ları
src/main/resources/
├── gui/                      # MainView.fxml, HashTabloView.fxml
└── style.css
```
