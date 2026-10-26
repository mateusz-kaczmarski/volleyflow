import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

let nextGaugeId = 0;

@Component({
  selector: 'vf-percentage-gauge',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="gauge" [class.signed]="kind() === 'signed'" [class.no-data]="value() === null"
      [style.--gauge-color]="color()" role="img" [attr.aria-label]="label() + ': ' + displayValue()">
      @if (kind() === 'ring') {
        <div class="ring">
          <svg viewBox="0 0 120 120" aria-hidden="true">
            <defs><linearGradient [attr.id]="gradientId" x1="0" y1="0" x2="1" y2="1">
              <stop offset="0%" [attr.stop-color]="color()" /><stop offset="100%" stop-color="#f8fafc" />
            </linearGradient></defs>
            <circle class="track" cx="60" cy="60" r="46" />
            <circle class="fill" cx="60" cy="60" r="46" pathLength="100"
              [attr.stroke]="'url(#' + gradientId + ')'" [style.stroke-dasharray]="amount() + ' 100'" />
          </svg>
          <strong>{{ displayValue() }}</strong>
        </div>
      } @else {
        <div class="signed-heading"><span>{{ label() }}</span><strong>{{ displayValue() }}</strong></div>
        <div class="signed-track" aria-hidden="true">
          <span class="signed-fill" [class.negative]="(value() || 0) < 0"
            [style.left.%]="(value() || 0) < 0 ? 50 - amount() / 2 : 50" [style.width.%]="amount() / 2"></span>
          <i class="zero"></i>
          @if (value() !== null) { <b class="marker" [class.negative]="value()! < 0" [style.left.%]="50 + clampedValue() / 2"></b> }
        </div>
        <div class="signed-scale" aria-hidden="true"><span>−100%</span><span>0</span><span>+100%</span></div>
      }
      @if (kind() === 'ring') { <span class="ring-label">{{ label() }}</span> }
    </div>
  `,
  styles: `
    :host { display: block; min-width: 0; }
    * { box-sizing: border-box; }
    .gauge { --gauge-color: #ef4444; }
    .ring { position: relative; width: min(100%, 8rem); aspect-ratio: 1; margin: 0 auto; }
    svg { display: block; width: 100%; height: 100%; transform: rotate(-90deg); overflow: visible; }
    circle { fill: none; stroke-width: 10; }
    .track { stroke: var(--input); }
    .fill { stroke-linecap: round; transition: stroke-dasharray .65s ease; filter: drop-shadow(0 0 5px color-mix(in srgb, var(--gauge-color) 40%, transparent)); }
    @starting-style { .fill { stroke-dasharray: 0 100; } }
    .ring strong { position: absolute; inset: 0; display: grid; place-items: center; color: var(--text); font-size: clamp(1rem, 2vw, 1.45rem); font-weight: 900; letter-spacing: -.05em; }
    .ring-label { display: block; margin-top: .35rem; text-align: center; color: var(--muted); font-size: .7rem; }
    .signed { padding: .85rem .7rem .65rem; border: 1px solid var(--line); border-radius: .65rem; background: color-mix(in srgb, var(--input) 65%, var(--surface)); }
    .signed-heading { display: flex; align-items: center; justify-content: space-between; gap: .5rem; margin-bottom: .8rem; }
    .signed-heading span { color: var(--muted); font-size: .7rem; }
    .signed-heading strong { color: var(--text); font-size: 1.05rem; font-weight: 900; white-space: nowrap; }
    .signed-track { position: relative; height: .7rem; border-radius: 1rem; background: linear-gradient(90deg, color-mix(in srgb, #ef4444 10%, var(--surface)), var(--input) 50%, color-mix(in srgb, #22c55e 10%, var(--surface))); }
    .signed-fill { position: absolute; top: 0; height: 100%; border-radius: 1rem; background: linear-gradient(90deg, #16a34a, #4ade80); box-shadow: 0 0 .6rem rgb(34 197 94 / .25); transition: width .65s ease, left .65s ease; }
    .signed-fill.negative { background: linear-gradient(90deg, #f87171, #dc2626); box-shadow: 0 0 .6rem rgb(239 68 68 / .25); }
    @starting-style { .signed-fill { width: 0; left: 50%; } }
    .zero { position: absolute; top: -.2rem; bottom: -.2rem; left: 50%; width: 2px; background: var(--muted); transform: translateX(-50%); }
    .marker { position: absolute; top: 50%; width: .7rem; height: .7rem; border: 2px solid #4ade80; border-radius: 50%; background: var(--surface); transform: translate(-50%, -50%); transition: left .65s ease; }
    .marker.negative { border-color: #f87171; }
    .signed-scale { display: flex; justify-content: space-between; margin-top: .5rem; color: var(--muted); font-size: .6rem; }
    .no-data .fill { stroke: var(--line); filter: none; }
    @media (prefers-reduced-motion: reduce) { .fill, .signed-fill, .marker { transition: none; } }
  `
})
export class PercentageGaugeComponent {
  readonly value = input.required<number | null>();
  readonly label = input.required<string>();
  readonly color = input('#ef4444');
  readonly kind = input<'ring' | 'signed'>('ring');
  readonly gradientId = `statistics-gauge-${nextGaugeId++}`;
  readonly clampedValue = computed(() => Math.max(-100, Math.min(100, this.value() || 0)));
  readonly amount = computed(() => this.kind() === 'signed' ? Math.abs(this.clampedValue()) : Math.max(0, this.clampedValue()));
  readonly displayValue = computed(() => this.value() === null ? '—' : `${this.value()}%`);
}
