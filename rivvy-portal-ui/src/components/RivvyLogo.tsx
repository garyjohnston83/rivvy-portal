interface RivvyLogoProps {
  size?: 'small' | 'large';
}

function RivvyLogo({ size = 'large' }: RivvyLogoProps) {
  const isLarge = size === 'large';

  const logoStyles: Record<string, React.CSSProperties> = {
    container: {
      textAlign: 'center',
      userSelect: 'none',
    },
    image: {
      width: isLarge ? '200px' : '80px',
      height: 'auto',
      display: isLarge ? 'block' : 'inline-block',
      margin: isLarge ? '0 auto' : undefined,
    },
  };

  return (
    <div style={logoStyles.container}>
      <img
        src="/rivvy_studios_logo.png"
        alt="Rivvy Studios"
        style={logoStyles.image}
      />
    </div>
  );
}

export default RivvyLogo;
