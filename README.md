# Şifre Üreteci v2

React (Vite) arayüz + Java (Spring Boot 2.7) REST API.
Parolanın güvenliği Deletes: `java.security.SecureRandom` **sunucuda** üretilir;
istemci şifre üretmez, sadece gösterir/kopyalar. Şifreler hiçbir yerde saklanmaz.

## Özellikler

- Tek üretim + **5'li toplu üretim** (sunucu tarafında)
- Uzunluk 6-64; A-Z / a-z / 0-9 / sembol seçimleri
- Karışan karakterleri (0/O/o, 1/l/I, B/8, |) dışlama
- **Entropi raporu** (bit) her yanıtta
- (Bonus uç) `POST /passwords/strength` — mevcut bir şifrenin yaklaşık gücü

## Çalıştırma

```bash
cd backend && mvn spring-boot:run     # API :8080
cd frontend && npm install && npm run dev   # UI :5173 (proxy → 8080)
```

## API

| Metot | Yol | Açıklama |
|---|---|---|
| POST | `/api/passwords/generate` | `{length, upper, lower, digits, symbols, excludeAmbiguous}` → `{password, poolSize, entropyBits}` |
| POST | `/api/passwords/generate/batch` | `{...aynı, count: 1-20}` → liste |
| POST | `/api/passwords/strength` | `{password}` → `{charsetClasses, approxBits, verdict}` |

Geçersiz istek (length 4-64 dışı, sıfır karakter kümesi) → 400.

v1 tek dosyalık HTML sürümü git geçmişinde korunmaktadır.
