#!/usr/bin/env python3
# SPDX-License-Identifier: GPL-3.0-or-later
"""
Gera dados de teste para o Mapa da Loja:
  - teste-mapa.zip                 backup com 1 seção (foto desenhada) e 1 produto
  - codigo-de-barras-teste.png     código de barras do produto, para bipar da tela

Uso:  python3 tools/gerar_dados_teste.py <pasta-de-saida>
Depois: copie o .zip para o celular e use "Restaurar backup" no app.
Precisa do Pillow (pip install pillow).
"""
import json, os, random, zipfile, sys
from PIL import Image, ImageDraw, ImageFont
out = sys.argv[1] if len(sys.argv) > 1 else "."
os.makedirs(f"{out}/teste/fotos", exist_ok=True)
random.seed(7)

# ---------- EAN-13 de teste (prefixo 200 = uso interno, não colide com produto real) ----------
base = "200000000001"
s = sum(int(d) * (3 if i % 2 else 1) for i, d in enumerate(base))
ean = base + str((10 - s % 10) % 10)
L = ["0001101","0011001","0010011","0111101","0100011","0110001","0101111","0111011","0110111","0001011"]
G = ["0100111","0110011","0011011","0100001","0011101","0111001","0000101","0010001","0001001","0010111"]
R = ["".join("1" if c == "0" else "0" for c in x) for x in L]
PAR = ["LLLLLL","LLGLGG","LLGGLG","LLGGGL","LGLLGG","LGGLLG","LGGGLL","LGLGLG","LGLGGL","LGGLGL"][int(ean[0])]
bits = "101" + "".join((L if PAR[i] == "L" else G)[int(d)] for i, d in enumerate(ean[1:7])) + "01010" + "".join(R[int(d)] for d in ean[7:]) + "101"
m = 6
bc = Image.new("RGB", ((len(bits) + 22) * m, 420), "white")
d = ImageDraw.Draw(bc)
for i, b in enumerate(bits):
    if b == "1":
        d.rectangle([(i + 11) * m, 30, (i + 12) * m - 1, 330], fill="black")
fnt = ImageFont.load_default(size=44)
d.text((bc.width // 2, 375), ean, fill="black", font=fnt, anchor="mm")
bc.save(f"{out}/codigo-de-barras-teste.png")

# ---------- foto "de mentira" de uma gôndola com 5 prateleiras ----------
W, H = 1200, 1600
img = Image.new("RGB", (W, H), (235, 232, 228))
d = ImageDraw.Draw(img)
d.rectangle([40, 40, W - 40, H - 40], fill=(250, 250, 250), outline=(120, 120, 120), width=8)
d.rectangle([40, 40, W - 40, 150], fill=(198, 40, 40))
d.text((W // 2, 95), "VITAMINAS  (TESTE)", fill="white", font=ImageFont.load_default(size=60), anchor="mm")
cores = [(46,125,50),(21,101,192),(239,108,0),(106,27,154),(0,131,143),(194,24,91),(249,168,37),(93,64,55)]
topo, fundo, n = 170, H - 60, 5
alt = (fundo - topo) / n
alvo = None
for k in range(n):
    y_base = topo + alt * (k + 1)
    d.rectangle([40, y_base - 18, W - 40, y_base], fill=(140, 140, 140))  # a prateleira
    x = 70
    j = 0
    while x < W - 140:
        w = random.randint(90, 150); h = random.randint(int(alt * 0.45), int(alt * 0.8))
        cor = random.choice(cores)
        d.rounded_rectangle([x, y_base - 18 - h, x + w, y_base - 22], radius=10, fill=cor, outline=(40, 40, 40), width=3)
        d.rectangle([x + 12, y_base - 18 - h + 25, x + w - 12, y_base - 18 - h + 55], fill="white")
        if k == 1 and j == 3:
            alvo = ((x + w / 2) / W, (y_base - 18 - h / 2) / H)
        x += w + 18; j += 1
img.save(f"{out}/teste/fotos/teste.jpg", quality=88)

dados = {
    "versao": 1,
    "secoes": [{"id": 1, "nome": "Teste - Vitaminas", "localizacao": "Corredor 1, lado direito", "foto": "teste.jpg"}],
    "produtos": [{"id": 1, "codigoBarras": ean, "codigoInterno": "123456", "nome": "PRODUTO TESTE - Vitamina C 1g",
                  "secaoId": 1, "prateleira": 2, "posX": alvo[0], "posY": alvo[1],
                  "observacao": "Produto de teste, pode apagar", "atualizadoEm": 0}],
}
with zipfile.ZipFile(f"{out}/teste-mapa.zip", "w") as z:
    z.writestr("dados.json", json.dumps(dados))
    z.write(f"{out}/teste/fotos/teste.jpg", "fotos/teste.jpg")
print(ean, alvo)
