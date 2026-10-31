import pygame
import random

colors = [
    (0, 0, 0),
    (120, 37, 179),
    (100, 179, 179),
    (80, 34, 22),
    (80, 134, 22),
    (180, 34, 22),
    (180, 34, 122),
]


class Figure:
    x = 0
    y = 0

    figures = [
        [[1, 5, 9, 13], [4, 5, 6, 7]],
        [[4, 5, 9, 10], [2, 6, 5, 9]],
        [[6, 7, 9, 10], [1, 5, 6, 10]],
        [[1, 2, 5, 9], [0, 4, 5, 6], [1, 5, 9, 8], [4, 5, 6, 10]],
        [[1, 2, 6, 10], [5, 6, 7, 9], [2, 6, 10, 11], [3, 5, 6, 7]],
        [[1, 4, 5, 6], [1, 4, 5, 9], [4, 5, 6, 9], [1, 5, 6, 9]],
        [[1, 2, 5, 6]],
    ]

    def __init__(self, x, y):
        self.x = x
        self.y = y
        self.type = random.randint(0, len(self.figures) - 1)
        self.color = random.randint(1, len(colors) - 1)
        self.rotation = 0

    def image(self):
        return self.figures[self.type][self.rotation]

    def rotate(self):
        self.rotation = (self.rotation + 1) % len(self.figures[self.type])


class Tetris:
    def __init__(self, height, width):
        self.level = 2
        self.score = 0
        self.state = "start"
        self.field = []
        self.height = 0
        self.width = 0
        self.x = 100
        self.y = 60
        self.zoom = 20
        self.figure = None

        self.height = height
        self.width = width
        self.field = []
        self.score = 0
        self.state = "start"
        for i in range(height):
            new_line = []
            for j in range(width):
                new_line.append(0)
            self.field.append(new_line)

    def new_figure(self):
        self.figure = Figure(3, 0)

    def intersects(self):
        intersection = False
        for i in range(4):
            for j in range(4):
                if i * 4 + j in self.figure.image():
                    if i + self.figure.y > self.height - 1 or \
                            j + self.figure.x > self.width - 1 or \
                            j + self.figure.x < 0 or \
                            self.field[i + self.figure.y][j + self.figure.x] > 0:
                        intersection = True
        return intersection

    def break_lines(self):
        lines = 0
        for i in range(1, self.height):
            zeros = 0
            for j in range(self.width):
                if self.field[i][j] == 0:
                    zeros += 1
            if zeros == 0:
                lines += 1
                for i1 in range(i, 1, -1):
                    for j in range(self.width):
                        self.field[i1][j] = self.field[i1 - 1][j]
        self.score += lines ** 2

    def go_space(self):
        while not self.intersects():
            self.figure.y += 1
        self.figure.y -= 1
        self.freeze()

    def go_down(self):
        self.figure.y += 1
        if self.intersects():
            self.figure.y -= 1
            self.freeze()

    def freeze(self):
        for i in range(4):
            for j in range(4):
                if i * 4 + j in self.figure.image():
                    self.field[i + self.figure.y][j + self.figure.x] = self.figure.color
        self.break_lines()
        self.new_figure()
        if self.intersects():
            self.state = "gameover"

    def go_side(self, dx):
        old_x = self.figure.x
        self.figure.x += dx
        if self.intersects():
            self.figure.x = old_x

    def rotate(self):
        old_rotation = self.figure.rotation
        self.figure.rotate()
        if self.intersects():
            self.figure.rotation = old_rotation


# Initialize the game engine
pygame.init()
pygame.mixer.init()


# --- SMART MUSIC MANAGER ---
# We create a custom function so we don't have to copy/paste the safety net!
def play_track(track_name, loop=-1):
    try:
        pygame.mixer.music.load(track_name)
        pygame.mixer.music.set_volume(1.0)
        pygame.mixer.music.play(loop)
    except (pygame.error, FileNotFoundError):
        print(f"Warning: {track_name} not found. Playing silently.")


# ---------------------------

# Define some colors
BLACK = (0, 0, 0)
WHITE = (255, 255, 255)
GRAY = (128, 128, 128)
GREEN = (34, 139, 34)

size = (400, 500)
# Make the window act like a normal desktop app with draggable edges and scaling
screen = pygame.display.set_mode(size, pygame.RESIZABLE | pygame.SCALED)

pygame.display.set_caption("Tetris")

# Fonts
font = pygame.font.SysFont('Calibri', 25, True, False)
font1 = pygame.font.SysFont('Calibri', 65, True, False)

# Loop until the user clicks the close button.
done = False
clock = pygame.time.Clock()
fps = 25
game = Tetris(20, 10)
counter = 0

pressing_down = False

# MENU & PAUSE VARIABLES
in_menu = True
is_paused = False
button_rect = pygame.Rect(100, 380, 200, 50)
game_over_played = False

# 1. PLAY MENU MUSIC WHEN APP BOOTS
play_track("menu.mp3", loop=-1)

while not done:
    if game.figure is None:
        game.new_figure()
    counter += 1
    if counter > 100000:
        counter = 0

    if not in_menu and not is_paused:
        if counter % (fps // game.level // 2) == 0 or pressing_down:
            if game.state == "start":
                game.go_down()

    # 2. TRIGGER GAME OVER MUSIC
    if game.state == "gameover" and not game_over_played:
        play_track("gameover.mp3", loop=0)  # loop=0 means it only plays exactly once!
        game_over_played = True

    for event in pygame.event.get():
        if event.type == pygame.QUIT:
            done = True

        # GLOBAL KEY BINDS (Works everywhere)
        if event.type == pygame.KEYDOWN:
            if event.key == pygame.K_f:
                pygame.display.toggle_fullscreen()

        if in_menu:
            # Scale mouse position relative to the window size so the button works
            if event.type == pygame.MOUSEBUTTONDOWN:
                if button_rect.collidepoint(event.pos):
                    in_menu = False
                    is_paused = False
                    game_over_played = False
                    game.__init__(20, 10)

                    # 3. PLAY IN-GAME MUSIC WHEN CLICKING START
                    play_track("background_hum.mp3", loop=-1)

        else:
            if event.type == pygame.KEYDOWN:
                if event.key == pygame.K_m:
                    in_menu = True
                    is_paused = False

                    # 4. PLAY MENU MUSIC WHEN RETURNING TO HOME SCREEN
                    play_track("menu.mp3", loop=-1)

                if event.key == pygame.K_p:
                    if game.state == "start":
                        is_paused = not is_paused

                if not is_paused and game.state == "start":
                    if event.key == pygame.K_UP:
                        game.rotate()
                    if event.key == pygame.K_DOWN:
                        pressing_down = True
                    if event.key == pygame.K_LEFT:
                        game.go_side(-1)
                    if event.key == pygame.K_RIGHT:
                        game.go_side(1)
                    if event.key == pygame.K_SPACE:
                        game.go_space()

                if event.key == pygame.K_r:
                    game.__init__(20, 10)
                    is_paused = False
                    game_over_played = False

                    # 5. PLAY IN-GAME MUSIC WHEN RESTARTING
                    play_track("background_hum.mp3", loop=-1)

            if event.type == pygame.KEYUP:
                if event.key == pygame.K_DOWN:
                    pressing_down = False

    screen.fill(WHITE)

    # DRAWING THE MENU
    if in_menu:
        title_text = font1.render("TETRIS", True, BLACK)
        screen.blit(title_text, (110, 50))

        instructions = [
            "How to Play:",
            "UP: Rotate   |   L/R: Move",
            "DOWN: Soft Drop | SPACE: Hard",
            "P: Pause   |   M: Main Menu",
            "R: Restart  |  F: Fullscreen"
        ]

        y_offset = 140
        for line in instructions:
            inst_surface = font.render(line, True, GRAY)
            screen.blit(inst_surface, (15, y_offset))
            y_offset += 40

        pygame.draw.rect(screen, GREEN, button_rect)
        pygame.draw.rect(screen, BLACK, button_rect, 2)

        btn_text = font.render("START GAME", True, WHITE)
        text_rect = btn_text.get_rect(center=button_rect.center)
        screen.blit(btn_text, text_rect)

    # DRAWING THE GAME
    else:
        for i in range(game.height):
            for j in range(game.width):
                pygame.draw.rect(screen, GRAY, [game.x + game.zoom * j, game.y + game.zoom * i, game.zoom, game.zoom],
                                 1)
                if game.field[i][j] > 0:
                    pygame.draw.rect(screen, colors[game.field[i][j]],
                                     [game.x + game.zoom * j + 1, game.y + game.zoom * i + 1, game.zoom - 2,
                                      game.zoom - 1])

        if game.figure is not None:
            for i in range(4):
                for j in range(4):
                    p = i * 4 + j
                    if p in game.figure.image():
                        pygame.draw.rect(screen, colors[game.figure.color],
                                         [game.x + game.zoom * (j + game.figure.x) + 1,
                                          game.y + game.zoom * (i + game.figure.y) + 1,
                                          game.zoom - 2, game.zoom - 2])

        text = font.render("Score: " + str(game.score), True, BLACK)
        screen.blit(text, [0, 0])

        # DRAWING PAUSE TEXT
        if is_paused:
            overlay = pygame.Surface((size[0], size[1]), pygame.SRCALPHA)
            overlay.fill((0, 0, 0, 150))
            screen.blit(overlay, (0, 0))

            text_paused = font1.render("PAUSED", True, WHITE)
            screen.blit(text_paused, text_paused.get_rect(center=(size[0] / 2, size[1] / 2)))

        # DRAWING GAME OVER AND FINAL SCORE
        if game.state == "gameover":
            overlay = pygame.Surface((size[0], size[1]), pygame.SRCALPHA)
            overlay.fill((0, 0, 0, 200))
            screen.blit(overlay, (0, 0))

            text_game_over = font1.render("GAME OVER", True, (0, 220, 255))
            text_final_score = font.render("Final Score: " + str(game.score), True, (255, 215, 0))
            text_restart = font.render("Press R to Restart", True, WHITE)
            text_menu = font.render("Press M for Main Menu", True, WHITE)

            # Neatly spaced lines for the Game Over screen
            screen.blit(text_game_over, text_game_over.get_rect(center=(size[0] / 2, 160)))
            screen.blit(text_final_score, text_final_score.get_rect(center=(size[0] / 2, 230)))
            screen.blit(text_restart, text_restart.get_rect(center=(size[0] / 2, 285)))
            screen.blit(text_menu, text_menu.get_rect(center=(size[0] / 2, 325)))

    pygame.display.flip()
    clock.tick(fps)

pygame.quit()