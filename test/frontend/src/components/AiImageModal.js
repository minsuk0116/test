// React 훅 임포트
import React, { useState } from 'react';
// HTTP 요청 라이브러리
import axios from 'axios';

/**
 * AiImageModal 컴포넌트
 * AI 이미지 생성을 위한 모달 창 컴포넌트
 * 
 * Props:
 * - isOpen: 모달 열림/닫힘 상태
 * - onClose: 모달 닫기 콜백 함수
 * - onImageGenerated: 이미지 생성 완료 시 호출될 콜백 함수
 * 
 * 기능:
 * - 사용자 프롬프트 입력
 * - 다양한 이미지 스타일 선택
 * - AI 이미지 생성 API 호출
 * - 생성된 이미지 미리보기
 */
const AiImageModal = ({ isOpen, onClose, onImageGenerated }) => {
  // 사용자 입력 프롬프트 (이미지 설명)
  const [prompt, setPrompt] = useState('');
  // 선택된 이미지 스타일
  const [style, setStyle] = useState('realistic');
  // 이미지 생성 중인지 여부 (로딩 상태)
  const [isGenerating, setIsGenerating] = useState(false);

  /**
   * 사용 가능한 이미지 스타일 옵션들
   * 각 스타일마다 아이콘, 레이블, 설명을 포함
   */
  const styleOptions = [
    { value: 'realistic', label: '🔍 사실적', description: '실제 사진과 같은 현실적인 스타일' },
    { value: 'cartoon', label: '🎨 만화적', description: '만화나 애니메이션 같은 스타일' },
    { value: 'artistic', label: '🖼️ 예술적', description: '회화나 예술작품 같은 스타일' },
    { value: 'watercolor', label: '🎭 수채화', description: '부드러운 수채화 스타일' },
    { value: 'oil_painting', label: '🖌️ 유화', description: '클래식한 유화 스타일' },
    { value: 'digital_art', label: '💻 디지털아트', description: '현대적인 디지털 아트 스타일' }
  ];

  /**
   * AI 이미지 생성 핸들러 함수
   * 사용자 입력과 선택된 스타일을 기반으로 이미지 생성 요청
   */
  const handleGenerate = async () => {
    // 빈 프롬프트 체크
    if (!prompt.trim()) {
      alert('원하는 이미지 내용을 입력해주세요.');
      return;
    }

    setIsGenerating(true); // 생성 중 상태 시작
    
    try {
      // 스타일 정보를 포함한 프롬프트 생성
      const styledPrompt = createStyledPrompt(prompt, style);
      
      // AI 이미지 생성 API 호출
      const response = await axios.post('http://localhost:8080/api/ai/generate-image', {
        prompt: styledPrompt
      });
      
      if (response.data.success === 'true') {
        onImageGenerated(response.data.imageUrl);
        handleClose();
        alert('AI 이미지가 생성되었습니다!');
      } else {
        alert('이미지 생성에 실패했습니다.');
      }
    } catch (error) {
      console.error('AI 이미지 생성 실패:', error);
      alert('이미지 생성 중 오류가 발생했습니다.');
    } finally {
      setIsGenerating(false);
    }
  };

  const createStyledPrompt = (userPrompt, selectedStyle) => {
    const stylePrompts = {
      realistic: `${userPrompt}, photorealistic, high quality, detailed, professional photography`,
      cartoon: `${userPrompt}, cartoon style, animated, colorful, fun, illustration`,
      artistic: `${userPrompt}, artistic, fine art, masterpiece, creative, expressive`,
      watercolor: `${userPrompt}, watercolor painting, soft colors, artistic, flowing, delicate`,
      oil_painting: `${userPrompt}, oil painting, classic art style, rich colors, textured, painterly`,
      digital_art: `${userPrompt}, digital art, modern, clean, vibrant, contemporary style`
    };

    return stylePrompts[selectedStyle] || stylePrompts.realistic;
  };

  const handleClose = () => {
    setPrompt('');
    setStyle('realistic');
    setIsGenerating(false);
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div style={{
      position: 'fixed',
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      backgroundColor: 'rgba(0, 0, 0, 0.5)',
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      zIndex: 1000
    }}>
      <div style={{
        backgroundColor: 'white',
        borderRadius: '12px',
        padding: '24px',
        width: '90%',
        maxWidth: '500px',
        maxHeight: '80vh',
        overflowY: 'auto',
        boxShadow: '0 10px 30px rgba(0, 0, 0, 0.3)'
      }}>
        {/* 헤더 */}
        <div style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: '20px',
          borderBottom: '2px solid #f1f3f4',
          paddingBottom: '12px'
        }}>
          <h3 style={{
            margin: 0,
            fontSize: '20px',
            fontWeight: '600',
            color: '#2c3e50'
          }}>
            🎨 AI 이미지 생성
          </h3>
          <button
            onClick={handleClose}
            disabled={isGenerating}
            style={{
              backgroundColor: 'transparent',
              border: 'none',
              fontSize: '24px',
              cursor: isGenerating ? 'not-allowed' : 'pointer',
              color: '#6c757d',
              padding: '4px',
              borderRadius: '4px'
            }}
          >
            ✕
          </button>
        </div>

        {/* 프롬프트 입력 */}
        <div style={{ marginBottom: '20px' }}>
          <label style={{
            display: 'block',
            marginBottom: '8px',
            fontWeight: '500',
            color: '#495057'
          }}>
            원하는 이미지 내용을 입력하세요:
          </label>
          <textarea
            value={prompt}
            onChange={(e) => setPrompt(e.target.value)}
            placeholder="예: 아름다운 석양이 지는 바다, 귀여운 고양이, 미래 도시 등"
            disabled={isGenerating}
            style={{
              width: '100%',
              minHeight: '80px',
              padding: '12px',
              border: '2px solid #e9ecef',
              borderRadius: '8px',
              fontSize: '14px',
              fontFamily: 'inherit',
              resize: 'vertical',
              outline: 'none',
              transition: 'border-color 0.3s ease'
            }}
            onFocus={(e) => e.target.style.borderColor = '#3498db'}
            onBlur={(e) => e.target.style.borderColor = '#e9ecef'}
          />
        </div>

        {/* 스타일 선택 */}
        <div style={{ marginBottom: '24px' }}>
          <label style={{
            display: 'block',
            marginBottom: '12px',
            fontWeight: '500',
            color: '#495057'
          }}>
            그림 스타일을 선택하세요:
          </label>
          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))',
            gap: '8px'
          }}>
            {styleOptions.map((option) => (
              <button
                key={option.value}
                onClick={() => setStyle(option.value)}
                disabled={isGenerating}
                style={{
                  padding: '12px',
                  border: style === option.value ? '2px solid #3498db' : '2px solid #e9ecef',
                  borderRadius: '8px',
                  backgroundColor: style === option.value ? '#e8f4fd' : '#ffffff',
                  cursor: isGenerating ? 'not-allowed' : 'pointer',
                  transition: 'all 0.3s ease',
                  textAlign: 'left'
                }}
              >
                <div style={{
                  fontWeight: '500',
                  fontSize: '13px',
                  color: style === option.value ? '#2980b9' : '#2c3e50',
                  marginBottom: '4px'
                }}>
                  {option.label}
                </div>
                <div style={{
                  fontSize: '11px',
                  color: '#6c757d',
                  lineHeight: '1.3'
                }}>
                  {option.description}
                </div>
              </button>
            ))}
          </div>
        </div>

        {/* 버튼들 */}
        <div style={{
          display: 'flex',
          gap: '12px',
          justifyContent: 'flex-end'
        }}>
          <button
            onClick={handleClose}
            disabled={isGenerating}
            style={{
              padding: '10px 20px',
              border: '2px solid #6c757d',
              borderRadius: '6px',
              backgroundColor: 'white',
              color: '#6c757d',
              cursor: isGenerating ? 'not-allowed' : 'pointer',
              fontWeight: '500',
              transition: 'all 0.3s ease'
            }}
          >
            취소
          </button>
          <button
            onClick={handleGenerate}
            disabled={isGenerating || !prompt.trim()}
            style={{
              padding: '10px 20px',
              border: 'none',
              borderRadius: '6px',
              backgroundColor: isGenerating || !prompt.trim() ? '#95a5a6' : '#28a745',
              color: 'white',
              cursor: isGenerating || !prompt.trim() ? 'not-allowed' : 'pointer',
              fontWeight: '500',
              transition: 'all 0.3s ease',
              minWidth: '100px'
            }}
          >
            {isGenerating ? '🔄 생성 중...' : '✨ 생성하기'}
          </button>
        </div>

        {/* 안내 메시지 */}
        <div style={{
          marginTop: '16px',
          padding: '12px',
          backgroundColor: '#f8f9fa',
          borderRadius: '6px',
          fontSize: '12px',
          color: '#6c757d',
          lineHeight: '1.4'
        }}>
          💡 <strong>팁:</strong> 구체적이고 상세한 설명을 입력하면 더 좋은 이미지를 생성할 수 있습니다.
          <br />
          ⏱️ 이미지 생성에는 약 10-30초 정도 소요됩니다.
        </div>
      </div>
    </div>
  );
};

export default AiImageModal;